package blog.typed.scaladsl

import scala.io.StdIn

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Actor
import akka.actor.typed.ActorRef

object Greeter2 {
  sealed trait Command
  case object Greet extends Command
  final case class WhoToGreet(who: String) extends Command

  val greeterBehavior: Behavior[Command] = greeterBehavior(currentGreeting = "hello")

  private def greeterBehavior(currentGreeting: String): Behavior[Command] =
    Actor.immutable[Command] { (ctx, msg) =>
      msg match {
        case WhoToGreet(who) =>
          greeterBehavior(s"hello, $who")
        case Greet =>
          println(currentGreeting)
          Actor.same
      }
    }
}

object HelloWorldApp2 {
  def main(args: Array[String]): Unit = {
    val root = Actor.deferred[Nothing] { ctx =>
      import Greeter2._
      val greeter: ActorRef[Command] = ctx.spawn(greeterBehavior, "greeter")
      greeter ! WhoToGreet("World")
      greeter ! Greet

      Actor.empty
    }
    val system = ActorSystem[Nothing](root, "HelloWorld")
    try {
      println("Press ENTER to exit the system")
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
