package projectZoom.connector

import java.io.{File, ByteArrayInputStream}
import models.Artifact
import projectZoom.core.event.{EventPublisher, Event}
import models.ArtifactInfo
import projectZoom.core.artifact._


trait ArtifactAggregatorActor extends ConnectorActor with EventPublisher {

  def publishFoundArtifact(file: Array[Byte], artifact: ArtifactInfo) = {
    publish(ArtifactFound(new ByteArrayInputStream(file), artifact))
  }

  def publishDeletedArtifact(artifact: ArtifactInfo) = {
    publish(ArtifactDeleted(artifact))
  }

  def publishAggregatedArtifacts(l: List[Tuple2[Array[Byte], ArtifactInfo]]) = {
    val foundArtifacts = for (pair <- l) yield ArtifactFound(new ByteArrayInputStream(pair._1), pair._2)
    publish(ArtifactAggregation("", foundArtifacts))
  }
}