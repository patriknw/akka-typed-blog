package blog.typed.javadsl;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Actor;

public abstract class Worker {
  private Worker() {
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
    return Actor.immutable(Command.class)
      .onMessage(Job.class, (ctx, msg) -> {
        System.out.println("Worker " + ctx.getSelf() + " got job " + msg.payload);
        return Actor.same();
      })
      .build();
  }

}