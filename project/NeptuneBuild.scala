import sbt._
import sbt.Keys._

object NeptuneBuild extends Build {

  lazy val neptune = Project(
    id = "neptune",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Neptune",
      organization := "org.jarsonmar",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1"
    )
  )
}
