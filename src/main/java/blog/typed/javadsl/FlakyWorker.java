package blog.typed.javadsl;

import java.util.concurrent.ThreadLocalRandom;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Actor;

public abstract class FlakyWorker {
  private FlakyWorker() {
  }

  interface Command {
  }

  public static class Job implements Command {
    public final String payload;

    public Job(String payload) {
      this.payload = payload;
    }
  }

  public static Behavior<Command> behavior() {
    return active(1);
  }

  private static Behavior<Command> active(int count) {
    return Actor.immutable(Command.class)
      .onMessage(Job.class, (ctx, msg) -> {
        if (ThreadLocalRandom.current().nextInt(5) == 0)
          throw new RuntimeException("Bad luck");

        ctx.getSystem().log().info("Worker {} got job {}, count {}",
          ctx.getSelf(), msg.payload, count);
        return active(count + 1);
      })
      .build();
  }

}