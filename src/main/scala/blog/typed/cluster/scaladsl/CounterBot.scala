package blog.typed.cluster.scaladsl

import scala.concurrent.duration._
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Actor

object CounterBot {

  sealed trait BotMessage
  private case object Tick extends BotMessage
  private case class GotCount(n: Int) extends BotMessage

  def behavior: Behavior[BotMessage] =
    Actor.deferred[BotMessage] { ctx ⇒
      val counter = ctx.spawn(Counter.behavior, "counter")

      val replyAdapter: ActorRef[Int] = ctx.spawnAdapter(GotCount.apply)

      Actor.withTimers { timers ⇒

        timers.startPeriodicTimer(Tick, Tick, 1.second)

        Actor.immutable[BotMessage] { (ctx, msg) ⇒
          msg match {
            case Tick ⇒
              println(s"Bot ${ctx.self} sending Increase and GetValue")
              counter ! Counter.Increment
              counter ! Counter.GetValue(replyAdapter)
              Actor.same
            case GotCount(n) ⇒
              println(s"Bot ${ctx.self} got $n")
              Actor.same
          }
        }
      }
    }

}
