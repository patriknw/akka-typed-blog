package blog.typed.cluster.scaladsl

import java.io.File
import java.util.concurrent.CountDownLatch

import akka.actor.ActorSystem
import akka.persistence.cassandra.testkit.CassandraLauncher
import akka.actor.typed.Props
import akka.cluster.sharding.typed.{ClusterSharding, ClusterShardingSettings}
import akka.actor.typed.scaladsl.adapter._
import blog.typed.persistence.scaladsl.{BlogCommand, BlogPost, PassivatePost}
import com.typesafe.config.{Config, ConfigFactory}

object ClusterShardingBlogPostApp {

  def main(args: Array[String]): Unit = {
    args.headOption match {

      case None =>
        startClusterInSameJvm()

      case Some(portString) if portString.matches("""\d+""") =>
        val port = portString.toInt
        startNode(port)

      case Some("cassandra") =>
        startCassandraDatabase()
        println("Started Cassandra, press Ctrl + C to kill")
        new CountDownLatch(1).await()

    }
  }

  def startClusterInSameJvm(): Unit = {
    startCassandraDatabase()

    startNode(2551)
    startNode(2552)
    startNode(2553)
  }

  def startNode(port: Int): Unit = {
    val system = ActorSystem("ClusterSystem", config(port))
    val typedSystem = system.toTyped

    ClusterSharding(typedSystem).spawn[BlogCommand](
      behavior = BlogPost.shardingBehavior,
      props = Props.empty,
      typeKey = BlogPost.ShardingTypeName,
      settings = ClusterShardingSettings(typedSystem),
      maxNumberOfShards = BlogPost.MaxNumberOfShards,
      handOffStopMessage = PassivatePost)

    if (port == 2551)
      system.spawn(BlogPostBot.behavior, "bot")
  }

  def config(port: Int): Config =
    ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port=$port
    """).withFallback(ConfigFactory.load("cluster.conf"))

  /**
   * To make the sample easier to run we kickstart a Cassandra instance to
   * act as the journal. Cassandra is a great choice of backend for Akka Persistence but
   * in a real application a pre-existing Cassandra cluster should be used.
   */
  def startCassandraDatabase(): Unit = {
    val databaseDirectory = new File("target/cassandra-db")
    CassandraLauncher.start(
      databaseDirectory,
      CassandraLauncher.DefaultTestConfigResource,
      clean = false,
      port = 9042)

    // shut the cassandra instance down when the JVM stops
    sys.addShutdownHook {
      CassandraLauncher.stop()
    }
  }

}
