name := "akka-typed-blog"

version := "1.0"

scalaVersion := "2.12.3"

enablePlugins(ProtobufPlugin)

lazy val akkaVersion = "2.5.9-SNAPSHOT"
lazy val cassandraPluginVersion = "0.80-RC2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,

  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraPluginVersion,
  // this allows us to start cassandra from the sample
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % cassandraPluginVersion,

  "com.typesafe.akka" %% "akka-testkit-typed" % akkaVersion  % "test",

  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
