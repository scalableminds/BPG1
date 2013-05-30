package projectZoom.connector

import java.io.File
import models.ArtifactLike
import projectZoom.core.event.{EventPublisher, Event}

case class ArtifactFound(file: File, artifact: ArtifactLike) extends Event
case class ArtifactDeleted(artifact: ArtifactLike) extends Event
case class ArtifactAggregation(l: List[ArtifactFound]) extends Event

trait ArtifactAggregatorActor extends ConnectorActor with EventPublisher {

  def publishFoundArtifact(file: File, artifact: ArtifactLike) = {
    publish(ArtifactFound(file, artifact))
  }

  def publishDeletedArtifact(artifact: ArtifactLike) = {
    publish(ArtifactDeleted(artifact))
  }

  def publishAggregatedArtifacts(l: List[Tuple2[File, ArtifactLike]]) = {
    val foundArtifacts = for (pair <- l) yield ArtifactFound(pair._1, pair._2)
    publish(ArtifactAggregation(foundArtifacts))
  }
}