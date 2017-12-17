package blog.typed.javadsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import scala.concurrent.duration.FiniteDuration;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Actor;
import akka.actor.typed.javadsl.TimerScheduler;

public abstract class Buncher {
  private Buncher() {
  }

  interface Msg {
  }

  public static final class Batch {
    private final List<Msg> messages;

    public Batch(List<Msg> messages) {
      this.messages = Collections.unmodifiableList(messages);
    }

    public List<Msg> getMessages() {
      return messages;
    }
  }

  private static final Object TIMER_KEY = new Object();

  private static class Timeout implements Msg {
  }

  public static Behavior<Msg> behavior(ActorRef<Batch> target, FiniteDuration after, int maxSize) {
    return Actor.withTimers(timers -> idle(timers, target, after, maxSize));
  }

  private static Behavior<Msg> idle(TimerScheduler<Msg> timers, ActorRef<Batch> target,
      FiniteDuration after, int maxSize) {
    return Actor.immutable(Msg.class)
      .onMessage(Msg.class, (ctx, msg) -> {
        timers.startSingleTimer(TIMER_KEY, new Timeout(), after);
        List<Msg> buffer = new ArrayList<>();
        buffer.add(msg);
        return active(buffer, timers, target, after, maxSize);
      })
      .build();
  }

  private static Behavior<Msg> active(List<Msg> buffer, TimerScheduler<Msg> timers,
      ActorRef<Batch> target, FiniteDuration after, int maxSize) {
    return Actor.immutable(Msg.class)
      .onMessage(Timeout.class, (ctx, msg) -> {
        target.tell(new Batch(buffer));
        return idle(timers, target, after, maxSize);
      })
      .onMessage(Msg.class, (ctx, msg) -> {
        buffer.add(msg);
        if (buffer.size() == maxSize) {
          timers.cancel(TIMER_KEY);
          target.tell(new Batch(buffer));
          return idle(timers, target, after, maxSize);
        } else {
          return active(buffer, timers, target, after, maxSize);
        }
      })
      .build();
    }

}