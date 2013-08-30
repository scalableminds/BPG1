import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "bp"
    val appVersion      = "0.1"

    val appDependencies = Seq(
   "org.mongodb" %% "casbah-commons" % "2.5.0",
    "org.mongodb" %% "casbah-core" % "2.5.0",
    "org.mongodb" %% "casbah-query" % "2.5.0",
    "org.mongodb" %% "casbah-gridfs" % "2.5.0",
    "com.novus" %% "salat-core" % "1.9.2"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += "repo.novus rels" at "http://repo.novus.com/releases/",
      resolvers += "repo.novus snaps" at "http://repo.novus.com/snapshots/"
    )
}
