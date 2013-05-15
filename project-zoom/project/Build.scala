import sbt._
import Keys._
import com.typesafe.config._
import PlayProject._

object ApplicationBuild extends Build {

  val conf = ConfigFactory.parseFile(new File("conf/application.conf"))

  val appName = conf.getString("application.name").toLowerCase
  val appVersion = "%s.%s.%s".format(
    conf.getString("application.major"),
    conf.getString("application.minor"),
    conf.getString("application.revision"))

  val projectZoomDependencies = Seq(
    "org.reactivemongo" %% "play2-reactivemongo" % "0.9",
    "org.reactivemongo" %% "reactivemongo-bson-macros" % "0.9",
    "org.mongodb" %% "casbah-commons" % "2.5.0",
    "org.mongodb" %% "casbah-core" % "2.5.0",
    "org.mongodb" %% "casbah-query" % "2.5.0",
    "com.restfb" % "restfb" % "1.6.11",
    "commons-io" % "commons-io" % "1.3.2",
    "org.apache.commons" % "commons-email" % "1.2",
    "com.typesafe.akka" %% "akka-agent" % "2.1.0",
    "org.jvnet.hudson" % "ganymed-ssh-2" % "build260",
    "org.apache.tika" % "tika-bundle" % "1.3",
    "com.scalableminds" %% "securesocial" % "master-SNAPSHOT" withSources ())

  val dependencyResolvers = Seq(
    Resolver.url("Scalableminds SNAPS Repo", url("http://scalableminds.github.com/snapshots/"))(Resolver.ivyStylePatterns),
    Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
    Resolver.url("typesafe community snaps", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
    "repo.novus rels" at "http://repo.novus.com/releases/",
    "repo.novus snaps" at "http://repo.novus.com/snapshots/",
    "sonatype rels" at "https://oss.sonatype.org/content/repositories/releases/",
    "sonatype snaps" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "sgodbillon" at "https://bitbucket.org/sgodbillon/repository/raw/master/snapshots/")

  lazy val common = play.Project(appName + "-common", appVersion, projectZoomDependencies, path = file("modules/common")).settings(
    //templatesImport += "",
    coffeescriptOptions := Seq("native", "coffee -p"),
    resolvers ++= dependencyResolvers //playAssetsDirectories += file("")
    )

  lazy val admin = play.Project(appName + "-admin", appVersion, projectZoomDependencies, path = file("modules/admin")).settings(
    //templatesImport += "",
    coffeescriptOptions := Seq("native", "coffee -p"),
    resolvers ++= dependencyResolvers //playAssetsDirectories += file("")
    ).dependsOn(common).aggregate(common)

  lazy val main = play.Project(appName + "-main", appVersion, projectZoomDependencies, path = file("modules/main")).settings(
    //templatesImport += "",
    coffeescriptOptions := Seq("native", "coffee -p"),
    resolvers ++= dependencyResolvers //playAssetsDirectories += file("")
    ).dependsOn(common).aggregate(common)

  val projectZoom = play.Project(appName, appVersion, projectZoomDependencies).settings(
    //templatesImport += "",
    coffeescriptOptions := Seq("native", "coffee -p"),
    resolvers ++= dependencyResolvers //playAssetsDirectories += file("")
    ).dependsOn(admin, main).aggregate(admin, main)

}
