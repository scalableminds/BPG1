package models
import com.mongodb.casbah.Imports._

case class DPhase(name: String, contentHistory: List[ObjectId], _id: ObjectId = new ObjectId) extends DAOCaseClass[DPhase]{
  val dao = DPhase
  val id = _id.toString()
  def content = (Entry.findOneById(contentHistory.head)).map(_.content) getOrElse ""
  
  def withAddedEntry(content: String) = this.copy(contentHistory = Entry.create(content)._id :: contentHistory)
}

object DPhase extends BasicDAO[DPhase]("dphases"){
  def create(name: String) = insertOne(DPhase(name, List(Entry.create()._id)))
  
  val names = List("General", "Understand", "Observe", "PointOfView", "Ideate", "Prototype", "Test")
  
  def addEntry(phase: DPhase, content: String) = insertOne(phase.update(_.withAddedEntry(content)))
}



