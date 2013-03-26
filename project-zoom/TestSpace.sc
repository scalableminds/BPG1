object TestSpace {
  import projectZoom.json._
  import akka.actor.ActorRef
  import akka.actor.Actor
  import akka.agent.Agent
  import scala.reflect.runtime.universe._
  import scala.reflect._



  trait Inception extends Event

  case class Test(penis: String) extends Inception
  case class Lala(penis: String) extends Event

  var m = Map[ClassTag[_ <: Event], List[ActorRef]]()

  def f[T <: Event]()(implicit t: ClassTag[T]) = {
    val x: Event = Test("asd")
    m += (t -> null)
    m.find {
      case (tag, act) =>
        tag.runtimeClass.isInstance(x)
    }
  }

  println("started")

  f[Inception]()
  m
}