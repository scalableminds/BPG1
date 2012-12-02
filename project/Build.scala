import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "bp"
    val appVersion      = "0.1"

    val appDependencies = Seq(
      "org.mongodb" %% "casbah" % "2.4.0",
      "com.novus" %% "salat" % "1.9.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += "repo.novus rels" at "http://repo.novus.com/releases/",
      resolvers += "repo.novus snaps" at "http://repo.novus.com/snapshots/"
    )
}
