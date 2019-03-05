scalaVersion := "2.12.7"

name := "rover"
organization := "com.rover"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

enablePlugins(JavaAppPackaging)