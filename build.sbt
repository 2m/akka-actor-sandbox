scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.7"
)

updateOptions := updateOptions.value.withConsolidatedResolution(true)
