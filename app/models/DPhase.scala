package models



abstract class DPhase{
  val dao = DPhase
  val content: String
}

case class Understand(content: String) extends DPhase
case class Observe(content: String) extends DPhase
case class PointOfView(content: String) extends DPhase
case class Ideate(content: String) extends DPhase
case class Prototype(content: String) extends DPhase
case class Test(content: String) extends DPhase


object DPhase extends BasicDAO[DPhase]("dphases"){
	
}