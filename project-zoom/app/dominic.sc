object dominic {
	import projectZoom.thumbnails.TextThumbnailJavaPlugin
	import akka.actor._
	
	/*
	val system = ActorSystem("mysystem")
	val thumbnailboy = system.actorOf(Props[ThumbnailActor], "thumbnailActor")
	thumbnailboy ! 5
	
  println("Welcome to the Scala worksheet")
  */
  
  val plugin = new TextThumbnailJavaPlugin()      //> plugin  : projectZoom.thumbnails.TextThumbnailJavaPlugin = projectZoom.thumb
                                                  //| nails.TextThumbnailJavaPlugin@c1f10e
  plugin.onResourceFound()                        //> Hesdfllo res0: <error> = ()
  //plugin.onResourceFound();
  
  
}