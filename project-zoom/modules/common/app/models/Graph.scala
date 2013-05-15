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

case class Position(x: Int, y: Int)

case class NodePayload(id: String, typ: String)

case class Node(id: Int, position: Position, payload: NodePayload)

case class Edge(from: Int, to: Int, comment: Option[String])

case class Cluster(id: Int, positions: List[Position])

case class Graph(
  id: String,
  group: Int,
  version: Int,
  nodes: List[Node],
  edges: List[Edge],
  clusters: List[Cluster])

trait PayloadTransformers {

  def payloadTypMapping(implicit ctx: DBAccessContext): Map[String, String => Future[Option[JsValue]]] = Map(
    "project" -> ProjectDAO.findOneByName _,
    "artifact" -> ArtifactDAO.findOneById _)

  implicit val nodePayloadFormat: Format[NodePayload] = Json.format[NodePayload]
}

trait GraphTransformers extends PayloadTransformers {
  implicit val positionFormat: Format[Position] = Json.format[Position]
  implicit val nodeFormat: Format[Node] = Json.format[Node]
  implicit val edgeFormat: Format[Edge] = Json.format[Edge]
  implicit val clusterFormat: Format[Cluster] = Json.format[Cluster]
  implicit val graphFormat: OFormat[Graph] = Json.format[Graph]

  val reducePayloadToId =
    (__ \ 'payload).json.update((__ \ 'id).json.pick)

  def transformNode(node: JsValue)(implicit ctx: DBAccessContext) = {
    node
      .asOpt[Node]
      .map { node: Node =>
        payloadTypMapping
          .get(node.payload.typ)
          .map(_(node.payload.id))
          .getOrElse(Future.successful(None))
      }
      .getOrElse(Future.successful(None))
  }

  def includePayloadDetails(implicit ctx: DBAccessContext) =
    (__ \ 'nodes).json.update(
      of[JsArray].map {
        case JsArray(list) => {
          Await.result(Future
            .sequence(list.map { jsNode: JsValue =>
              transformNode(jsNode)
            })
            .map(l => JsArray(l.flatten)), 5 seconds)
        }
      })
}

object GraphDAO extends SecuredMongoJsonDAO with GraphTransformers {
  val collectionName = "graphs"
}