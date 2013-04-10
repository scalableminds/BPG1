object dominic {

  
	import akka.actor._
	
	import projectZoom.thumbnails.text.TextThumbnailPlugin
	
	/*
	val system = ActorSystem("mysystem")
	val thumbnailboy = system.actorOf(Props[ThumbnailActor], "thumbnailActor")
	thumbnailboy ! 5
	
  println("Welcome to the Scala worksheet")
  */
  
  val plugin = new TextThumbnailPlugin()          //> plugin  : projectZoom.thumbnails.text.TextThumbnailPlugin = projectZoom.thum
                                                  //| bnails.text.TextThumbnailPlugin@5e176f

  val s = plugin.onResourceFound()                //> onResourceFound called SLF4J: Class path contains multiple SLF4J bindings.
                                                  //| SLF4J: Found binding in [jar:file:/home/user/dev/BPG1/project-zoom/playframe
                                                  //| work/repository/cache/ch.qos.logback/logback-classic/jars/logback-classic-1.
                                                  //| 0.9.jar!/org/slf4j/impl/StaticLoggerBinder.class]
                                                  //| SLF4J: Found binding in [jar:file:/home/user/tika-app-1.3.jar!/org/slf4j/imp
                                                  //| l/StaticLoggerBinder.class]
                                                  //| SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanat
                                                  //| ion.
                                                  //| SLF4J: Actual binding is of type [ch.qos.logback.classic.util.ContextSelecto
                                                  //| rStaticBinder]
                                                  //| s  : String = "
                                                  //| Receipt
                                                  //| Account billed: scalableminds (tomh@scalableminds.com)
                                                  //| Transaction ID: bcw7cj
                                                  //| 
                                                  //| Date Plan Price
                                                  //| 
                                                  //| 11/04/12 06:39PM PST Bronze USD $25.00*
                                                  //| 
                                                  //| GitHub, Inc.
                                                  //| 548 4th St.
                                                  //| San Francisco, CA 94107
                                                  //| 
                                                  //| Web: http://github.com/contact
                                                  //| Email: support@github.com
                                                  //| 
                                                  //| * EU customers: prices exclude VAT
                                                  //| 
                                                  //| 
                                                  //| "
  val l = s.length() > 5                          //> l  : Boolean = true

  //plugin.onResourceFound();
  
  
  
}