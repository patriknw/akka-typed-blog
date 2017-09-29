package blog.typed.persistence.scaladsl

import akka.Done
import akka.actor.Status
import akka.typed.Behavior
import akka.typed.persistence.scaladsl.PersistentActor
import akka.typed.persistence.scaladsl.PersistentActor._

final class Post3 {

  def behavior: Behavior[BlogCommand] =
    PersistentActor.immutable[BlogCommand, BlogEvent, BlogState](
      persistenceId = "abc",
      initialState = BlogState.empty,
      actions,
      applyEvent)

  private val actions: Actions[BlogCommand, BlogEvent, BlogState] =
    Actions { (ctx, cmd, state) ⇒
      cmd match {
        case AddPost(postId, content, replyTo) ⇒
          val evt = PostAdded(postId, content)
          Persist[BlogEvent, BlogState](evt).andThen { state2 ⇒
            // After persist is done additional side effects can be performed
            replyTo ! AddPostDone(postId)
          }
        case ChangeBody(postId, newBody, replyTo) ⇒
          val evt = BodyChanged(postId, newBody)
          Persist[BlogEvent, BlogState](evt).andThen { _ ⇒
            replyTo ! Done
          }
        case Publish(postId, replyTo) ⇒
          Persist[BlogEvent, BlogState](Published(postId)).andThen { _ ⇒
            replyTo ! Done
          }
        case GetPost(_, replyTo) ⇒
          replyTo ! state.content.get
          PersistNothing()
      }
    }

  private def applyEvent(event: BlogEvent, state: BlogState): BlogState =
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
