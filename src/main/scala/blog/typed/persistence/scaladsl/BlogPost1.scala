package blog.typed.persistence.scaladsl

import akka.typed.Behavior
import akka.typed.persistence.scaladsl.PersistentActor

object BlogPost1 {

  def behavior: Behavior[BlogCommand] =
    PersistentActor.immutable[BlogCommand, BlogEvent, BlogState](
      persistenceId = "abc",
      initialState = BlogState.empty,
      commandHandler = PersistentActor.CommandHandler { (ctx, state, cmd) ⇒ ??? },
      eventHandler = (state, evt) ⇒ ???)

}

