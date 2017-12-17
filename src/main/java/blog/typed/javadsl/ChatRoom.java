package blog.typed.javadsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Actor;

public abstract class ChatRoom {
  private ChatRoom() {
  }

  static interface Command {}
  public static final class GetSession implements Command {
    public final String screenName;
    public final ActorRef<SessionEvent> replyTo;
    public GetSession(String screenName, ActorRef<SessionEvent> replyTo) {
      this.screenName = screenName;
      this.replyTo = replyTo;
    }
  }

  private static final class PostSessionMessage implements Command {
    public final String screenName;
    public final String message;
    public PostSessionMessage(String screenName, String message) {
      this.screenName = screenName;
      this.message = message;
    }
  }

  static interface SessionEvent {}
  public static final class SessionGranted implements SessionEvent {
    public final ActorRef<PostMessage> handle;
    public SessionGranted(ActorRef<PostMessage> handle) {
      this.handle = handle;
    }
  }
  public static final class SessionDenied implements SessionEvent {
    public final String reason;
    public SessionDenied(String reason) {
      this.reason = reason;
    }
  }
  public static final class MessagePosted implements SessionEvent {
    public final String screenName;
    public final String message;
    public MessagePosted(String screenName, String message) {
      this.screenName = screenName;
      this.message = message;
    }
  }

  public static final class PostMessage {
    public final String message;
    public PostMessage(String message) {
      this.message = message;
    }
  }

  public static Behavior<Command> behavior() {
    return chatRoom(new ArrayList<ActorRef<SessionEvent>>());
  }

  private static Behavior<Command> chatRoom(List<ActorRef<SessionEvent>> sessions) {
    return Actor.immutable(Command.class)
      .onMessage(GetSession.class, (ctx, getSession) -> {
        ActorRef<PostMessage> wrapper = ctx.spawnAdapter(p ->
          new PostSessionMessage(getSession.screenName, p.message));
        getSession.replyTo.tell(new SessionGranted(wrapper));
        List<ActorRef<SessionEvent>> newSessions =
          new ArrayList<ActorRef<SessionEvent>>(sessions);
        newSessions.add(getSession.replyTo);
        return chatRoom(newSessions);
      })
      .onMessage(PostSessionMessage.class, (ctx, post) -> {
        MessagePosted mp = new MessagePosted(post.screenName, post.message);
        sessions.forEach(s -> s.tell(mp));
        return Actor.same();
      })
      .build();
  }



}