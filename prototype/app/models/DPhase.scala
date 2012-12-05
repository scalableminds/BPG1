package models


case class DPhase(name: String, history : List[String] = List("")) {
  def content = history.head
}



