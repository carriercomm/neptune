name := "Neptune"

version := "0.0.0"

scalaVersion := "2.10.0"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.1.0",
  "redis.clients" % "jedis" % "2.1.0",
  "org.slf4j" % "slf4j-simple" % "1.7.2",
  "org.apache.thrift" % "libthrift" % "0.9.0",
  "postgresql" % "postgresql" % "9.0-801.jdbc3"
)
