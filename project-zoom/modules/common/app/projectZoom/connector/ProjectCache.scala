package projectZoom.connector

import akka.agent._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import projectZoom.util.PlayActorSystem
import org.joda.time.DateTime
import models.ProjectLike
import play.Logger

object ProjectCache extends PlayActorSystem{
  private val cache = Agent[List[ProjectLike]](Nil)
  
  def getProjects = cache()
  
  def setProjects(l: List[ProjectLike]) = cache send l 
  
  private def currentSeason() = {
    val month = new DateTime().getMonthOfYear
    if(3 < month && month < 10) "ST" else "WT"
  }
  
  private def currentYear() = new DateTime().getYear()
  
  
  def getCurrentProjects = {
    getProjects.filter(_.year == currentYear).filter(_.season == currentSeason)
  }
  
  def getAllProjectsExistingBy(date: DateTime) = {
    getProjects.filter(project => date.compareTo(project.startDate) >= 0)
  }
}