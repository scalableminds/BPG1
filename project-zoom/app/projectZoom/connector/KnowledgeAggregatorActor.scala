package projectZoom.connector

import projectZoom.core.event.{EventPublisher, Event}
import models.{Project, User}

case class ProjectFound(project: Project) extends Event
case class ProjectAggregation(l: List[ProjectFound]) extends Event
case class UserFound(user: User) extends Event
case class UserAggregation(l: List[UserFound]) extends Event

class KnowledgeAggregatorActor extends ConnectorActor with EventPublisher {
  
}