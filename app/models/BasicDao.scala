package models

import com.mongodb.casbah.Imports._
import models.context._
import com.novus.salat.dao.SalatDAO
import org.bson.types.ObjectId
import com.mongodb.casbah.MongoDB

class BasicDAO[T <: AnyRef](collectionName: String)(implicit val m: Manifest[T])
    extends SalatDAO[T, ObjectId](collection = DB.connection(collectionName)) {

  def findAll = find(MongoDBObject.empty).toList

  def findOneById(id: String): Option[T] = {
    if (ObjectId.isValid(id))
      findOneById(new ObjectId(id))
    else
      None
  }

  def insertOne(el: T): T = {
    super.insert(el)
    el
  }
}