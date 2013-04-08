package projectZoom.connector

import java.io.File
import models.Artifact
import projectZoom.core.event.{EventPublisher, Event}

case class ArtifactFound(file: File, artifact: Artifact) extends Event
case class ArtifactDeleted(artifact: Artifact) extends Event
case class ArtifactAggregation(l: List[ArtifactFound]) extends Event

trait ArtifactAggregatorActor extends ConnectorActor with EventPublisher {

  def publishFoundArtifact(file: File, artifact: Artifact) = {
    publish(ArtifactFound(file, artifact))
  }

  def publishDeletedArtifact(artifact: Artifact) = {
    publish(ArtifactDeleted(artifact))
  }

  def publishAggregatedArtifacts(l: List[Tuple2[File, Artifact]]) = {
    val foundArtifacts = for (pair <- l) yield ArtifactFound(pair._1, pair._2)
    publish(ArtifactAggregation(foundArtifacts))
  }

}