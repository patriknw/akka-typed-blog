package blog.typed.javadsl;

import akka.typed.Behavior;
import akka.typed.javadsl.Actor;

public abstract class Gabbler {
  private Gabbler() {
  }

  public static Behavior<ChatRoom.SessionEvent> behavior() {
    return Actor.immutable(ChatRoom.SessionEvent.class)
      .onMessage(ChatRoom.SessionDenied.class, (ctx, msg) -> {
        System.out.println("cannot start chat room session: " + msg.reason);
        return Actor.stopped();
      })
      .onMessage(ChatRoom.SessionGranted.class, (ctx, msg) -> {
        msg.handle.tell(new ChatRoom.PostMessage("Hello World!"));
        return Actor.same();
      })
      .onMessage(ChatRoom.MessagePosted.class, (ctx, msg) -> {
        System.out.println("message has been posted by '" +
          msg.screenName +"': " + msg.message);
        return Actor.stopped();
      })
      .build();
  }

}