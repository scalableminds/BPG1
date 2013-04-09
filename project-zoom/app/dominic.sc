object dominic {
	import projectZoom.thumbnails._
	
	/*
	val system = ActorSystem("mysystem")
	val thumbnailboy = system.actorOf(Props[ThumbnailActor], "thumbnailActor")
	thumbnailboy ! 5
	
  println("Welcome to the Scala worksheet")
  */
  
  val plugin = new TextThumbnailJavaPlugin()      //> plugin  : projectZoom.thumbnails.TextThumbnailJavaPlugin = projectZoom.thumb
                                                  //| nails.TextThumbnailJavaPlugin@1d3c468a
  plugin.onResourceFound()
  //plugin.onResourceFound();
  
  
}