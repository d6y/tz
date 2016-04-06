name := "tz-codec"

version := "1.0.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.scodec" %% "scodec-bits" % "1.1.0"
libraryDependencies += "org.scodec" %% "scodec-core" % "1.9.0"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code",
  "-Xlint",
  "-Xfatal-warnings"
)

