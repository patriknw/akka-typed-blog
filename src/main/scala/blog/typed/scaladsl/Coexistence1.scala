package blog.typed.scaladsl

import scala.io.StdIn

import akka.typed.Behavior
import akka.typed.scaladsl.Actor.same

object Coexistence1 {
  import akka.typed.scaladsl.adapter._

  object MyUntyped1 {
    def props(): akka.actor.Props = akka.actor.Props(new MyUntyped1)
  }

  class MyUntyped1 extends akka.actor.Actor {

    // context.spawn is an implicit extension method
    val second: akka.typed.ActorRef[MyTyped1.Command] =
      context.spawn(MyTyped1.behavior, "second")

    // context.watch is an implicit extension method
    context.watch(second)

    // self can be used as the `replyTo` parameter here because
    // there is an implicit conversion from akka.actor.ActorRef to
    // akka.typed.ActorRef
    second ! MyTyped1.Ping(self)

    override def receive = {
      case MyTyped1.Pong =>
        println(s"$self got Pong from ${sender()}")
        // context.stop is an implicit extension method
        context.stop(second)
      case akka.actor.Terminated(ref) =>
        println(s"$self observed termination of $ref")
        context.stop(self)
    }

  }

  object MyTyped1 {
    sealed trait Command
    final case class Ping(replyTo: akka.typed.ActorRef[Pong.type]) extends Command
    case object Pong

    val behavior: Behavior[Command] =
      akka.typed.scaladsl.Actor.immutable { (ctx, msg) =>
        msg match {
          case Ping(replyTo) =>
            println(s"${ctx.self} got Ping from $replyTo")
            replyTo ! Pong
            same
        }
      }
  }

}

object CoexistenceApp1 {
  import Coexistence1._

  def main(args: Array[String]): Unit = {
    val system = akka.actor.ActorSystem("sys")
    system.actorOf(MyUntyped1.props(), "first")
    try {
      // Exit the system after ENTER is pressed
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
