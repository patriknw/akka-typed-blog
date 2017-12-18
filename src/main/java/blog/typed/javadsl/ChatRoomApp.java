package blog.typed.javadsl;

import java.io.IOException;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Actor;

public class ChatRoomApp {

  public static void main(String[] args) throws IOException {
    Behavior<Void> root = Actor.deferred(ctx -> {
      ActorRef<ChatRoom.Command> chatRoom =
        ctx.spawn(ChatRoom.behavior(), "chatRoom");
      ActorRef<ChatRoom.SessionEvent> gabbler =
          ctx.spawn(Gabbler.behavior(), "gabbler");
      chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler));

      return Actor.empty();
    });
    ActorSystem<Void> system = ActorSystem.create(root, "ChatRoomDemo");
    try {
      System.out.println("Press ENTER to exit the system");
      System.in.read();
    } finally {
      system.terminate();
    }
  }
}

