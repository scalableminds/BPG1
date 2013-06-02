package projectZoom.connector

import akka.agent._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import projectZoom.util.PlayActorSystem

import models.ProjectLike

object ProjectCache extends PlayActorSystem{
  private val cache = Agent[List[ProjectLike]](Nil)
  
  def getProjects = cache()
  
  def setProjects(l: List[ProjectLike]) = cache send l 
}