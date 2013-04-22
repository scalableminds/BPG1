package projectZoom.connector

import projectZoom.core.event.{EventPublisher, Event}
import models.Project
import projectZoom.core.knowledge.ProfileAggregation
import models.Profile
import projectZoom.core.knowledge.ProfileFound

trait KnowledgeAggregatorActor extends ConnectorActor with EventPublisher {
  
  def publishProfiles(l: List[Profile]) = {
    publish(ProfileAggregation(l.map(user => ProfileFound(user))))
  }
  
}