package blog.typed.persistence.scaladsl

import akka.typed.persistence.scaladsl.PersistentActor
import akka.typed.Behavior

object Post1 {

  def behavior: Behavior[BlogCommand] =
    PersistentActor.immutable[BlogCommand, BlogEvent, BlogState](
      persistenceId = "abc",
      initialState = BlogState.empty,
      actions = PersistentActor.Actions { (ctx, cmd, state) ⇒ ??? },
      applyEvent = (evt, state) ⇒ ???)

}

