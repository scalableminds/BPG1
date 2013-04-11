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

  val s = plugin.onResourceFound()                //> onResourceFound called java.lang.NoClassDefFoundError: org/eclipse/core/runt
                                                  //| ime/Assert
                                                  //| 	at org.eclipse.zest.cloudio.TagCloud.<init>(TagCloud.java:230)
                                                  //| 	at org.eclipse.zest.cloudio.TagCloud.<init>(TagCloud.java:267)
                                                  //| 	at projectZoom.thumbnails.text.TextThumbnailPlugin.createTagCloud(TextTh
                                                  //| umbnailPlugin.java:56)
                                                  //| 	at projectZoom.thumbnails.text.TextThumbnailPlugin.onResourceFound(TextT
                                                  //| humbnailPlugin.java:48)
                                                  //| 	at dominic$$anonfun$main$1.apply$mcV$sp(dominic.scala:18)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at dominic$.main(dominic.scala:6)
                                                  //| 	at dominic.main(dominic.scala)
                                                  //| Caused by: java.lang.ClassNotFoundException: org.eclipse.core.runtime.Assert
                                                  //| 
                                                  //| 	at java.net.URLClassLoader$1.run(URLClassLoader.java:202)
                                                  //| 	at java.security.AccessController.doPrivileged(Native Method)
                                                  //| 	at java.net.URLClassLoader.findClass(URLClassLoader.java:190)
                                                  //| 	at java.lang.ClassLoader.loadClass(ClassLoader.java:306)
                                                  //| 	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:301)
                                                  //| 	at java.lang.ClassLoader.loadClass(ClassLoader.java:247)
                                                  //| 	... 10 more
  val l = s.length() > 5

  //plugin.onResourceFound();
  
  
  
}