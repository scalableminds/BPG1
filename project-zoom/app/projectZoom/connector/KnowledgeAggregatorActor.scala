package projectZoom.connector

import projectZoom.core.event.{EventPublisher, Event}
import models.{Project, UserLike}

case class ProjectFound(project: Project) extends Event
case class ProjectAggregation(l: List[ProjectFound]) extends Event
case class UserFound(user: UserLike) extends Event
case class UserAggregation(l: List[UserFound]) extends Event

trait KnowledgeAggregatorActor extends ConnectorActor with EventPublisher {
  
  def publishUsers(l: List[UserLike]) = {
    publish(UserAggregation(l.map(user => UserFound(user))))
  }
  
}