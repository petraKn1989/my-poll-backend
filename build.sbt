name := "poll-backend"
organization := "com.pollapp"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.17"

// Play + Guice
libraryDependencies += guice

// Testy
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test

// Slick + Evolutions + PostgreSQL
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
  "org.postgresql" % "postgresql" % "42.6.0"   // PostgreSQL driver
)

// 🔧 Fix verze scala-xml
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
