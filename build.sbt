scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.9"
)

scalafmtConfig in ThisBuild := Some(file(".scalafmt"))
reformatOnCompileSettings
