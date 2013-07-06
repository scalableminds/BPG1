package projectZoom.connector.util

object Jaccard {
  def apply[A](X: Set[A], Y: Set[A]) = {
    (X intersect Y).size.toFloat / (X union Y).size
  }
}