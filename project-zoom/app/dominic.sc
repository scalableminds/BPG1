object dominic {
  
	import akka.actor._
	
	import projectZoom.thumbnails.text.TextThumbnailPlugin
	
	/*
	val system = ActorSystem("mysystem")
	val thumbnailboy = system.actorOf(Props[ThumbnailActor], "thumbnailActor")
	thumbnailboy ! 5
	
  println("Welcome to the Scala worksheet")
  */
  
  val plugin = new TextThumbnailPlugin()

  val s = plugin.onResourceFound()
  val l = s.length() > 5

  //plugin.onResourceFound();
}