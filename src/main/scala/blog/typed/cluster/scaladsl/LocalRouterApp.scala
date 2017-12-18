package blog.typed.cluster.scaladsl

import akka.actor.typed.scaladsl.adapter._

object LocalRouterApp {
  def main(args: Array[String]): Unit = {
    val system = akka.actor.ActorSystem("Sys")

    system.spawn(Routee.behavior, "routee1")
    system.spawn(Routee.behavior, "routee2")
    system.spawn(Routee.behavior, "routee3")

    system.spawn(RouterBot.behavior, "bot")

  }
}
