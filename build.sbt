name := "akka-typed-blog"

version := "1.0"

scalaVersion := "2.12.2"

lazy val akkaVersion = "2.5.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-typed-testkit" % akkaVersion  % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
