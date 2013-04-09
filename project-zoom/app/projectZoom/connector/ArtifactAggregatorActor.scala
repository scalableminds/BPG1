package projectZoom.connector

import java.io.File
import models.Artifact
import projectZoom.core.event.{EventPublisher, Event}
import models.ArtifactInfo

case class ArtifactFound(file: File, artifact: ArtifactInfo) extends Event
case class ArtifactDeleted(artifact: ArtifactInfo) extends Event
case class ArtifactAggregation(l: List[ArtifactFound]) extends Event

trait ArtifactAggregatorActor extends ConnectorActor with EventPublisher {

  def publishFoundArtifact(file: File, artifact: ArtifactInfo) = {
    publish(ArtifactFound(file, artifact))
  }

  def publishDeletedArtifact(artifact: ArtifactInfo) = {
    publish(ArtifactDeleted(artifact))
  }

  def publishAggregatedArtifacts(l: List[Tuple2[File, ArtifactInfo]]) = {
    val foundArtifacts = for (pair <- l) yield ArtifactFound(pair._1, pair._2)
    publish(ArtifactAggregation(foundArtifacts))
  }

}