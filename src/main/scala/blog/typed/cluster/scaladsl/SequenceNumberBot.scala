package blog.typed.cluster.scaladsl

import scala.concurrent.duration._
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Actor

object SequenceNumberBot {

  sealed trait BotMessage
  private case object Tick extends BotMessage
  private case class GotNumber(n: Long) extends BotMessage

  def behavior(generator: ActorRef[SequenceNumberGenerator.Next]): Behavior[BotMessage] =
    Actor.deferred[BotMessage] { ctx ⇒
      val replyAdapter: ActorRef[Long] = ctx.spawnAdapter(GotNumber.apply)

      Actor.withTimers { timers ⇒

        timers.startPeriodicTimer(Tick, Tick, 1.second)

        Actor.immutable[BotMessage] { (ctx, msg) ⇒
          msg match {
            case Tick ⇒
              println(s"Bot ${ctx.self} sending Next")
              generator ! SequenceNumberGenerator.Next(replyAdapter)
              Actor.same
            case GotNumber(n) ⇒
              println(s"Bot ${ctx.self} got $n")
              Actor.same
          }
        }
      }
    }

}
