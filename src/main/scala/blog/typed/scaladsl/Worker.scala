package blog.typed.scaladsl

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Actor

object Worker {
  sealed trait Command
  final case class Job(payload: String) extends Command

  val workerBehavior: Behavior[Command] =
    Actor.immutable[Command] { (ctx, msg) =>
      msg match {
        case Job(payload) =>
          println(s"Worker ${ctx.self} got job $payload")
          Actor.same
      }
    }
}