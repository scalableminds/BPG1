package models
import org.joda.time.DateTime
import com.mongodb.casbah.Imports._

case class Entry(content: String = "", _id: ObjectId = new ObjectId, creationDate: DateTime = new DateTime) extends DAOCaseClass[Entry] {
  val dao = Entry
  val id = _id.toString
}

object Entry extends BasicDAO[Entry]("entries") {
  def create(content: String = "") = insertOne(Entry(content))
}