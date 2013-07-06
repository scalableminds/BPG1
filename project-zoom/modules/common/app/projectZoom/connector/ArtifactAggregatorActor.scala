package projectZoom.connector

import java.io.File
import models.ArtifactLike
import projectZoom.core.event.{EventPublisher, Event}
import projectZoom.core.artifact._
import java.io.ByteArrayInputStream
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout

trait ArtifactAggregatorActor extends ConnectorActor with EventPublisher {

  implicit val timeout = Timeout(60 seconds)

  def projectCache = context.actorFor(s"${context.parent.path}/ProjectCache")
  
  def projects = projectCache ? ProjectsRequest
  
  def toInputStream(bytes: Array[Byte]) = new ByteArrayInputStream(bytes)
  
  def publishFoundArtifact(bytes: Array[Byte], artifact: ArtifactLike) = {
    publish(ArtifactFound(toInputStream(bytes), artifact))
  }

  def publishDeletedArtifact(artifact: ArtifactLike) = {
    publish(ArtifactDeleted(artifact))
  }

  def publishAggregatedArtifacts(l: List[Tuple2[Array[Byte], ArtifactLike]]) = {
    val foundArtifacts = for (pair <- l) yield ArtifactFound(toInputStream(pair._1), pair._2)
    publish(ArtifactAggregation("",foundArtifacts))
  }
}