package blog.typed.persistence.scaladsl

import akka.Done
import akka.actor.Status
import akka.typed.Behavior
import akka.typed.persistence.scaladsl.PersistentActor
import akka.typed.persistence.scaladsl.PersistentActor._

final class BlogPost2 {

  def behavior: Behavior[BlogCommand] =
    PersistentActor.immutable[BlogCommand, BlogEvent, BlogState](
      persistenceId = "abc",
      initialState = BlogState.empty,
      actions,
      applyEvent)

  private val actions: Actions[BlogCommand, BlogEvent, BlogState] =
    Actions { (ctx, cmd, state) ⇒
      cmd match {
        case AddPost(content, replyTo) ⇒
          val evt = PostAdded(content.postId, content)
          Persist(evt).andThen { state2 ⇒
            // After persist is done additional side effects can be performed
            replyTo ! AddPostDone(content.postId)
          }
        case ChangeBody(newBody, replyTo) ⇒
          val evt = BodyChanged(state.postId, newBody)
          Persist(evt).andThen { _ ⇒
            replyTo ! Done
          }
        case Publish(replyTo) ⇒
          Persist(Published(state.postId)).andThen { _ ⇒
            replyTo ! Done
          }
        case GetPost(replyTo) ⇒
          replyTo ! state.content.get
          PersistNothing()
        case PassivatePost =>
          Stop()
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
