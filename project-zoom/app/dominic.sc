object dominic {
  import projectZoom.thumbnails.text.TextThumbnailPlugin
  import akka.actor._
  
  /*
  val system = ActorSystem("mysystem")
  val thumbnailboy = system.actorOf(Props[ThumbnailActor], "thumbnailActor")
  thumbnailboy ! 5
  
  println("Welcome to the Scala worksheet")
  */
  
  val plugin = new TextThumbnailPlugin()
  plugin.onResourceFound()
  //plugin.onResourceFound();
  
   println("test")
}