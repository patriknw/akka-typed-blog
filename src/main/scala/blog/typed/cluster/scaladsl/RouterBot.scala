package blog.typed.cluster.scaladsl

import scala.concurrent.duration._
import akka.typed.ActorRef
import akka.typed.Behavior
import akka.typed.scaladsl.Actor

object RouterBot {

  sealed trait BotMessage
  private case object Tick extends BotMessage
  private case object GotPong extends BotMessage

  val behavior: Behavior[BotMessage] =
    Actor.deferred[BotMessage] { ctx ⇒
      val router = ctx.spawn(RandomRouter.clusterRouter(Routee.PingServiceKey), "pingRouter")
      val pongAdapter: ActorRef[Routee.Pong.type] = ctx.spawnAdapter(_ ⇒ GotPong)

      Actor.withTimers { timers ⇒

        timers.startPeriodicTimer(Tick, Tick, 1.second)

        Actor.immutable[BotMessage] { (ctx, msg) ⇒
          msg match {
            case Tick ⇒
              println(s"Bot ${ctx.self} sending ping")
              router ! Routee.Ping(pongAdapter)
              Actor.same
            case GotPong ⇒
              println(s"Bot ${ctx.self} got pong")
              Actor.same
          }
        }
      }
    }

}
