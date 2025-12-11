name := """poll-backend"""
organization := "com.pollapp"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.17"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
  "org.xerial" % "sqlite-jdbc" % "3.45.1.0"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
  "org.xerial" % "sqlite-jdbc" % "3.45.1.0",   // pro SQLite, můžeš odstranit, pokud nepoužíváš
  "org.postgresql" % "postgresql" % "42.6.0"   // <-- toto je nově pro Postgres
)


// 🔧 Fix verze scala-xml
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
