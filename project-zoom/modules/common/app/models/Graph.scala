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

case class Position(x: Int, y: Int)

case class NodePayload(id: String)

case class Node(id: Int, position: Position, typ: String, payload: NodePayload)

case class Edge(from: Int, to: Int, comment: Option[String])

case class Cluster(id: Int, waypoints: List[Position])

case class Graph(
  group: String,
  version: Int,
  nodes: List[Node],
  edges: List[Edge],
  clusters: List[Cluster],
  _id: BSONObjectID = BSONObjectID.generate)

trait PayloadTransformers {

  def payloadTypMapping(implicit ctx: DBAccessContext): Map[String, String => Future[Option[JsValue]]] = Map(
    "project" -> ProjectDAO.findOneByName _,
    "artifact" -> ArtifactDAO.findOneById _)

  implicit val nodePayloadFormat: Format[NodePayload] = Json.format[NodePayload]
}

trait GraphTransformers extends PayloadTransformers with MongoHelpers {
  implicit val positionFormat: Format[Position] = Json.format[Position]
  implicit val nodeFormat: Format[Node] = Json.format[Node]
  implicit val edgeFormat: Format[Edge] = Json.format[Edge]
  implicit val clusterFormat: Format[Cluster] = Json.format[Cluster]
  implicit val graphFormat: OFormat[Graph] = Json.format[Graph]

  val replacePayloadContentWithId =
    (__ \ 'payload).json.update((__ \ 'id).json.pick)

  val versionInfoReads =
    (__ \ 'version).json.pickBranch

  val incrementVersion =
    (__).json.update((__ \ 'version).json.copyFrom((__ \ 'version).json.pick[JsNumber].map {
      case JsNumber(n) => JsNumber(n + 1)
    }))
    
  def replacePayloadIdWithContent(content: JsValue) =
    (__).json.update((__ \ 'payload).json.put(content))

  def transformNode(node: JsValue)(implicit ctx: DBAccessContext): Future[JsValue] = {
    node
      .asOpt[Node]
      .map { node: Node =>
        payloadTypMapping
          .get(node.typ)
          .map(_(node.payload.id))
          .getOrElse(Future.successful(None))
      }
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
        case JsArray(list) => {
          Await.result(Future
            .sequence(list.map { jsNode: JsValue =>
              transformNode(jsNode)
            })
            .map(l => JsArray(l)), 5 seconds)
        }
      })
}

object GraphDAO extends SecuredMongoJsonDAO with GraphTransformers {
  val collectionName = "graphs"

  def extractVersionInfo(graph: Graph) = {
    (Json.toJson(graph) transform versionInfoReads).get
  }

  def generateEmptyGraph = {
    Graph(
      group = UUID.randomUUID().toString,
      version = 0,
      nodes = Nil,
      edges = Nil,
      clusters = Nil)
  }

  def findLatestForGroup(group: String)(implicit ctx: DBAccessContext) = {
    findMaxBy("version")
  }
}