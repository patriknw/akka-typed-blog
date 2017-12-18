package blog.typed.cluster.scaladsl

import akka.actor.ActorSystem
import akka.actor.typed.Props
import akka.cluster.typed.ClusterSingleton
import akka.cluster.typed.ClusterSingletonSettings
import akka.actor.typed.scaladsl.adapter._
import com.typesafe.config.{ Config, ConfigFactory }

object CounterApp {

  def main(args: Array[String]): Unit = {
    args.headOption match {

      case None =>
        startClusterInSameJvm()

      case Some(portString) if portString.matches("""\d+""") =>
        val port = portString.toInt
        startNode(port)

      case Some(other) =>
        throw new IllegalArgumentException(s"Unknown parameter $other")
    }
  }

  def startClusterInSameJvm(): Unit = {
    startNode(2551)
    startNode(2552)
    startNode(2553)
  }

  def startNode(port: Int): Unit = {
    val system = ActorSystem("ClusterSystem", config(port))

    system.spawn(CounterBot.behavior, "bot")
  }

  def config(port: Int): Config =
    ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port=$port
    """).withFallback(ConfigFactory.load("cluster.conf"))

}
