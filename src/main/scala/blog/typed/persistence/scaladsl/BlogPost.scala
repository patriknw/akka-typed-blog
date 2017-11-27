package blog.typed.persistence.scaladsl

import akka.Done
import akka.typed.Behavior
import akka.typed.cluster.sharding.EntityTypeKey
import akka.typed.persistence.scaladsl.PersistentActor
import akka.typed.persistence.scaladsl.PersistentActor._

object BlogPost {

  val ShardingTypeName = EntityTypeKey[BlogCommand]("BlogPost")
  val MaxNumberOfShards = 1000

  /**
   * An ordinary persistent actor with a fixed persistenceId,
   * see alternative `shardingBehavior` below.
   */
  def behavior: Behavior[BlogCommand] =
    PersistentActor.immutable[BlogCommand, BlogEvent, BlogState](
      persistenceId = "abc",
      initialState = BlogState.empty,
      commandHandler,
      eventHandler)

  /**
   * Persistent actor in Cluster Sharding, when the persistenceId is not known
   * until the actor is started and typically based on the entityId, which
   * is the actor name.
   */
  def shardingBehavior: Behavior[BlogCommand] =
    PersistentActor.persistentEntity[BlogCommand, BlogEvent, BlogState](
      persistenceIdFromActorName = name => ShardingTypeName.name + "-" + name,
      initialState = BlogState.empty,
      commandHandler,
      eventHandler)

  private def commandHandler: CommandHandler[BlogCommand, BlogEvent, BlogState] = CommandHandler.byState {
    case state if state.isEmpty  ⇒ initial
    case state if !state.isEmpty ⇒ postAdded
  }

  private def initial: CommandHandler[BlogCommand, BlogEvent, BlogState] =
    CommandHandler { (ctx, state, cmd) ⇒
      cmd match {
        case AddPost(content, replyTo) ⇒
          val evt = PostAdded(content.postId, content)
          Effect.persist(evt).andThen { state2 ⇒
            // After persist is done additional side effects can be performed
            replyTo ! AddPostDone(content.postId)
          }
        case PassivatePost =>
          Effect.stop
        case other ⇒
          Effect.unhandled
      }
    }

  private def postAdded: CommandHandler[BlogCommand, BlogEvent, BlogState] = {
    CommandHandler { (ctx, state, cmd) ⇒
      cmd match {
        case ChangeBody(newBody, replyTo) ⇒
          val evt = BodyChanged(state.postId, newBody)
          Effect.persist(evt).andThen { _ ⇒
            replyTo ! Done
          }
        case Publish(replyTo) ⇒
          Effect.persist(Published(state.postId)).andThen { _ ⇒
            println(s"Blog post ${state.postId} was published")
            replyTo ! Done
          }
        case GetPost(replyTo) ⇒
          replyTo ! state.content.get
          Effect.none
        case _: AddPost ⇒
          Effect.unhandled
        case PassivatePost =>
          Effect.stop
      }
    }
  }

  private def eventHandler(state: BlogState, event: BlogEvent): BlogState =
    event match {
      case PostAdded(postId, content) ⇒
        state.withContent(content)

      case BodyChanged(_, newBody) ⇒
        state.content match {
          case Some(c) ⇒ state.copy(content = Some(c.copy(body = newBody)))
          case None    ⇒ state
        }

      case Published(_) ⇒
        state.copy(published = true)
    }

}
