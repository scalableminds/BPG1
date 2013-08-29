package models

import play.api.libs.json.JsValue
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.Logger
import projectZoom.util.MongoHelpers
import reactivemongo.bson.BSONObjectID
import java.util.UUID
import play.modules.reactivemongo.json.BSONFormats._

/**
 * Two dimensional point
 */
case class Position(x: Int, y: Int)

/**
 * Payload reference
 */
case class NodePayload(id: String)

/**
 * A node consists of a 2D position and its payload. The typ defines where
 * the payload of the node gets resolved.
 */
case class Node(id: Int, position: Position, typ: String, payload: NodePayload, comment: Option[String] = None)

/**
 * An edge is a relation between two nodes
 */
case class Edge(from: Int, to: Int, comment: Option[String] = None)

/**
 * A cluster visually groups several nodes together
 */
case class Cluster(id: Int, waypoints: List[Position], content: List[Int], comment: Option[String] = None, phase: Option[String] = None)

/**
 * A graph defines a specific version at a specific time. If the graph gets
 * changed a new object with an incremented version field gets stored in the
 * database. All versions of a graph share the same group.
 */
case class Graph(
  group: String,
  version: Int,
  nodes: List[Node],
  edges: List[Edge],
  clusters: List[Cluster],
  _project: BSONObjectID,
  _id: BSONObjectID = BSONObjectID.generate)

/**
 * Operations to transform a nodes payload
 */
trait PayloadTransformers {

  /**
   * Every value used for node.typ needs to define a finder. It is used to 
   * resolve the stored reference and include the complete object into the 
   * graph.
   */
  def payloadTypMapping(implicit ctx: DBAccessContext): Map[String, String => Future[Option[JsValue]]] = Map(
    "project" -> ProjectDAO.findOneByName _,
    "artifact" -> ArtifactDAO.findOneById _)

  /**
   * Method to Read and write a payload from and to JSON
   */
  implicit val nodePayloadFormat: Format[NodePayload] = Json.format[NodePayload]

  /**
   * Transformer to reduce the payload information in a JSON object to its id
   */
  val replacePayloadContentWithId =
    (__ \ 'payload).json.update((__ \ 'id).json.pick)
    
  /**
   * Transformer to replace the id stored in the payload field with the given
   * content  
   */  
  def replacePayloadIdWithContent(content: JsValue) =
    (__).json.update((__ \ 'payload).json.put(content))
}

trait GraphTransformers extends PayloadTransformers with MongoHelpers {
  /**
   * JSON converters
   * ------------->
   */
  implicit val positionFormat: Format[Position] = Json.format[Position]
  implicit val nodeFormat: Format[Node] = Json.format[Node]
  implicit val edgeFormat: Format[Edge] = Json.format[Edge]
  implicit val clusterFormat: Format[Cluster] = Json.format[Cluster]
  implicit val graphFormat: OFormat[Graph] = Json.format[Graph]
  /**
   * <-------------
   */

  /**
   * Extract the version number from a JSON Object
   */
  val versionInfoReads =
    (__ \ 'version).json.pickBranch

  /**
   * Increment the version number in a JSON Object
   */
  val incrementVersion =
    (__).json.update((__ \ 'version).json.copyFrom((__ \ 'version).json.pick[JsNumber].map {
      case JsNumber(n) => JsNumber(n + 1)
    }))
    
  def payloadForNode(node: Node)(implicit ctx: DBAccessContext) =
    payloadTypMapping
      .get(node.typ)
      .map(dbObjetFinder => dbObjetFinder(node.payload.id))
      .getOrElse(Future.successful(None))
  /**
   * 
   */
  def transformToNodeWithPayload(node: JsValue)(implicit ctx: DBAccessContext): Future[JsValue] = {
    node
      .asOpt[Node]
      .map( node => payloadForNode(node))
      .getOrElse(Future.successful(None))
      .map {
        case Some(p) =>
          val beautifiedPayload = (p transform beautifyObjectId).get
          node.transform(replacePayloadIdWithContent(beautifiedPayload)).get
        case _ =>
          Logger.warn("Couldn't find node payload: " + node)
          node.transform(replacePayloadIdWithContent(Json.obj())).get
      }
  }

  def includePayloadDetails(implicit ctx: DBAccessContext) =
    (__ \ 'nodes).json.update(
      of[JsArray].map {
        case JsArray(nodes) => {
          Await.result(Future
            .sequence(nodes.map(transformToNodeWithPayload))
            .map(l => JsArray(l)), 5 seconds)
        }
      })
}

object GraphDAO extends SecuredMongoJsonDAO[Graph] with GraphTransformers {
  /**
   * Name of the DB collection
   */
  val collectionName = "graphs"

  def extractVersionInfo(graph: Graph) = {
    (Json.toJson(graph) transform versionInfoReads).get
  }

  def generateEmptyGraph(_project: String) = {
    BSONObjectID.parse(_project).map { id =>
      Graph(
        group = UUID.randomUUID().toString,
        version = 0,
        nodes = Nil,
        edges = Nil,
        clusters = Nil,
        _project = id)
    }
  }

  def findLatestForGroup(group: String)(implicit ctx: DBAccessContext) = {
    findMaxBy("version", Json.obj("group" -> group))
  }
}