name := """cluster-dummy"""
organization := "com.cluster"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.14"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.3.0",  // Adjust the version if necessary
  "com.typesafe.play" %% "play-slick-evolutions" % "5.3.0",  // For handling database evolutions
  "mysql" % "mysql-connector-java" % "8.0.33"  // MySQL driver (adjust version if necessary)
)
libraryDependencies += "com.github.cb372" %% "scalacache-core" % "0.28.0"
libraryDependencies += "com.github.cb372" %% "scalacache-caffeine" % "0.28.0"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.cluster.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.cluster.binders._"
