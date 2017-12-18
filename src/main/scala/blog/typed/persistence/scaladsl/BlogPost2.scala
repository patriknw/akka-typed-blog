package blog.typed.persistence.scaladsl

import akka.Done
import akka.actor.typed.Behavior
import akka.persistence.typed.scaladsl.PersistentActor
import akka.persistence.typed.scaladsl.PersistentActor._

final class BlogPost2 {

  def behavior: Behavior[BlogCommand] =
    PersistentActor.immutable[BlogCommand, BlogEvent, BlogState](
      persistenceId = "abc",
      initialState = BlogState.empty,
      commandHandler,
      eventHandler)

  private val commandHandler: CommandHandler[BlogCommand, BlogEvent, BlogState] =
    CommandHandler { (_, state, cmd) ⇒
      cmd match {
        case AddPost(content, replyTo) ⇒
          val evt = PostAdded(content.postId, content)
          Effect.persist(evt).andThen { state2 ⇒
            // After persist is done additional side effects can be performed
            replyTo ! AddPostDone(content.postId)
          }
        case ChangeBody(newBody, replyTo) ⇒
          val evt = BodyChanged(state.postId, newBody)
          Effect.persist(evt).andThen { _ ⇒
            replyTo ! Done
          }
        case Publish(replyTo) ⇒
          Effect.persist(Published(state.postId)).andThen { blog ⇒
            replyTo ! Done
          }
        case GetPost(replyTo) ⇒
          replyTo ! state.content.get
          Effect.none
        case PassivatePost =>
          Effect.stop
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
