package projectZoom.connector

import projectZoom.connector._
import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.Logger
import scala.util.Random

class TestActor(v: Int) extends ArtifactAggregatorActor{
  
  def aggregate() = {
    Logger.info(s"Testvalue: $v")
  }
  
  def start() = {
    Logger.info("TestActor started")
  }
  
  def stop = ???
  
}

object TestActor {
  
  def props: Props = {
    Props(() => new TestActor(Random.nextInt(100)))
  }
}