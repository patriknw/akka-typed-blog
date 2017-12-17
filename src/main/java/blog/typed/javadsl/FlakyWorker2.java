package blog.typed.javadsl;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.javadsl.Actor;

public abstract class FlakyWorker2 {
  private FlakyWorker2() {
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
    return Actor.deferred(ctx -> {
      ctx.getSystem().log().info("Worker {} is STARTED", ctx.getSelf());
      PrintWriter out = new PrintWriter(new FileWriter(
        "target/out-" + ctx.getSelf().path().name() + ".txt", true));
      return active(1, out);
    });
  }

  private static Behavior<Command> active(int count, PrintWriter out) {
    return Actor.immutable(Command.class)
      .onMessage(Job.class, (ctx, msg) -> {
        if (ThreadLocalRandom.current().nextInt(5) == 0)
          throw new RuntimeException("Bad luck");

        ctx.getSystem().log().info("Worker {} got job {}, count {}",
          ctx.getSelf(), msg.payload, count);
        out.println("Worker " + ctx.getSelf() + " got job " + msg.payload +
          ", count " + count);
        return active(count + 1, out);
      })
      .onSignalEquals(PreRestart.instance(), ctx -> {
        ctx.getSystem().log().info("Worker {} is RESTARTED, count {}", ctx.getSelf(), count);
        out.close();
        return Actor.same();
      })
      .onSignalEquals(PostStop.instance(), ctx -> {
        ctx.getSystem().log().info("Worker {} is STOPPED, count {}", ctx.getSelf(), count);
        out.close();
        return Actor.same();
      })
      .build();
  }

}