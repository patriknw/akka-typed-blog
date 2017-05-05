package blog.typed.scaladsl

import scala.io.StdIn

import akka.typed.ActorSystem
import akka.typed.Behavior
import akka.typed.scaladsl.Actor

object Greeter1 {
  sealed trait Command
  case object Greet extends Command
  final case class WhoToGreet(who: String) extends Command

  val greeterBehavior: Behavior[Command] =
    Actor.mutable[Command](ctx => new Greeter1)
}

class Greeter1 extends Actor.MutableBehavior[Greeter1.Command] {
  import Greeter1._

  private var greeting = "hello"

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case WhoToGreet(who) =>
        greeting = s"hello, $who"
      case Greet =>
        println(greeting)
    }
    this
  }
}

object HelloWorldApp1 {
  def main(args: Array[String]): Unit = {
    val root = Actor.deferred[Nothing] { ctx =>
      import Greeter1._
      val greeter = ctx.spawn(greeterBehavior, "greeter")
      greeter ! WhoToGreet("World")
      greeter ! Greet

      Actor.empty
    }
    val system = ActorSystem[Nothing]("HelloWorld", root)
    try {
      println("Press ENTER to exit the system")
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
