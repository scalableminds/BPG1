package projectZoom.connector

import projectZoom.core.event.{EventPublisher, Event}
import models.ProjectLike
import projectZoom.core.knowledge.ProfileAggregation
import models.Profile
import projectZoom.core.knowledge.ProfileFound
import projectZoom.core.knowledge.ProjectAggregation
import projectZoom.core.knowledge.ProjectFound

trait KnowledgeAggregatorActor extends ConnectorActor with EventPublisher {
  
  def publishProfiles(l: List[Profile]) = {
    publish(ProfileAggregation(l.map(user => ProfileFound(user))))
  }
  
  def publishProjects(l: List[ProjectLike]) = {
    publish(ProjectAggregation(l.map(project => ProjectFound(project))))
  }
}