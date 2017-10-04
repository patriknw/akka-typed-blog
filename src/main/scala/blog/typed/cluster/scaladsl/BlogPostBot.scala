package blog.typed.cluster.scaladsl

import scala.concurrent.duration._
import akka.typed.ActorRef
import akka.typed.Behavior
import akka.typed.scaladsl.Actor
import akka.typed.cluster.sharding.ClusterSharding
import java.util.UUID
import blog.typed.persistence.scaladsl.PostContent
import blog.typed.persistence.scaladsl.BlogPost
import akka.typed.cluster.sharding.EntityRef
import blog.typed.persistence.scaladsl.BlogCommand
import blog.typed.persistence.scaladsl.AddPost
import blog.typed.persistence.scaladsl.AddPostDone
import blog.typed.persistence.scaladsl.Publish
import akka.Done

object BlogPostBot {

  sealed trait BotMessage
  private case object Tick extends BotMessage
  private case class InternalAddPostDone(postId: String) extends BotMessage
  private case object InternalDone extends BotMessage

  def behavior(): Behavior[BotMessage] =
    Actor.deferred[BotMessage] { ctx ⇒

      val sharding = ClusterSharding(ctx.system)

      val addPostReplyAdapter: ActorRef[AddPostDone] =
        ctx.spawnAdapter(reply => InternalAddPostDone(reply.postId))
      val publishReplyAdapter: ActorRef[Done] =
        ctx.spawnAdapter(_ => InternalDone)

      Actor.withTimers { timers ⇒

        timers.startPeriodicTimer(Tick, Tick, 1.second)

        Actor.immutable[BotMessage] { (ctx, msg) ⇒
          msg match {
            case Tick ⇒
              val postId = UUID.randomUUID().toString
              val content = PostContent(postId, "Title...", "Body...")
              println(s"Bot ${ctx.self} adding post $postId")

              val entityRef: EntityRef[BlogCommand] =
                sharding.entityRefFor(BlogPost.ShardingTypeName, postId)

              entityRef ! AddPost(content, addPostReplyAdapter)
              Actor.same

            case InternalAddPostDone(postId) ⇒
              println(s"Bot ${ctx.self} publishing $postId")
              val entityRef = sharding.entityRefFor(BlogPost.ShardingTypeName, postId)
              entityRef ! Publish(publishReplyAdapter)
              Actor.same

            case InternalDone =>
              Actor.same
          }
        }
      }
    }

}
