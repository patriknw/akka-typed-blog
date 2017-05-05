package blog.classic.scaladsl

import scala.io.StdIn

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

object Greeter1 {
  case object Greet
  final case class WhoToGreet(who: String)
}

class Greeter1 extends Actor {
  import Greeter1._

  private var greeting = "hello"

  override def receive = {
    case WhoToGreet(who) =>
      greeting = s"hello, $who"
    case Greet =>
      println(greeting)
  }
}

object HelloWorldApp1 {
  import Greeter1._
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("HelloWorld")
    try {
      val greeter: ActorRef = system.actorOf(Props[Greeter1], "greeter")
      greeter ! WhoToGreet("World")
      greeter ! Greet

      println("Press ENTER to exit the system")
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
