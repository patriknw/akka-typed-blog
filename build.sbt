name := "akka-typed-blog"

version := "1.0"

scalaVersion := "2.12.3"
//scalaVersion := "2.11.11"

enablePlugins(ProtobufPlugin)

lazy val akkaVersion = "2.5.6"
lazy val akkaTypedVersion = "2.5.7-M1"
lazy val cassandraPluginVersion = "0.56"

resolvers += "Akka Snapshots" at "https://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-typed" % akkaTypedVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraPluginVersion,
  // this allows us to start cassandra from the sample
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % cassandraPluginVersion,
  
  
  "com.typesafe.akka" %% "akka-typed-testkit" % akkaTypedVersion  % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
