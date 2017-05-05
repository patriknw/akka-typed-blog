package blog.typed.scaladsl

import scala.io.StdIn

import akka.typed.Behavior
import akka.typed.scaladsl.Actor.{same, stopped}

object Coexistence2 {
  import akka.typed.scaladsl.adapter._

  object MyTyped2 {
    final case class Ping(replyTo: akka.typed.ActorRef[Pong.type])
    sealed trait Command
    case object Pong extends Command

    val behavior: Behavior[Command] =
      akka.typed.scaladsl.Actor.deferred { context =>
        // context.spawn is an implicit extension method
        val second: akka.actor.ActorRef =
          context.actorOf(MyUntyped2.props(), "second")

        // context.watch is an implicit extension method
        context.watch(second)

        // illustrating how to pass sender, toUntyped is an implicit extension method
        second.tell(MyTyped2.Ping(context.self), context.self.toUntyped)

        akka.typed.scaladsl.Actor.immutable[Command] { (ctx, msg) =>
          msg match {
            case Pong =>
              // it's not possible to get the sender, that must be sent in message
              println(s"${ctx.self} got Pong")
              // context.stop is an implicit extension method
              ctx.stop(second)
              same
          }
        } onSignal {
          case (ctx, akka.typed.Terminated(ref)) =>
            println(s"${ctx.self} observed termination of $ref")
            stopped
        }
      }
  }

  object MyUntyped2 {
    def props(): akka.actor.Props = akka.actor.Props(new MyUntyped2)
  }

  class MyUntyped2 extends akka.actor.Actor {

    override def receive = {
      case MyTyped2.Ping(replyTo) =>
        // we use the replyTo ActorRef in the message,
        // but could use sender() if needed and it was passed
        // as parameter to tell
        println(s"$self got Pong from ${sender()}")
        replyTo ! MyTyped2.Pong
    }
  }

}

object CoexistenceApp2 {
  import Coexistence2._

  def main(args: Array[String]): Unit = {
    import akka.typed.scaladsl.adapter._
    val system = akka.actor.ActorSystem("sys")
    // system.spawn is an implicit extension method
    system.spawn(MyTyped2.behavior, "first")
    try {
      // Exit the system after ENTER is pressed
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
