scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"   % "2.4.14",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.14",
  "org.scalatest"     %% "scalatest"    % "3.0.1" % Test
)

scalafmtConfig in ThisBuild := Some(file(".scalafmt"))
reformatOnCompileSettings

mainClass in reStart := Some("EcTerminate")
