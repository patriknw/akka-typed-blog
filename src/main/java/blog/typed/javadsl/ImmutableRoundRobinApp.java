package blog.typed.javadsl;

import java.io.IOException;

import akka.typed.ActorRef;
import akka.typed.ActorSystem;
import akka.typed.Behavior;
import akka.typed.javadsl.Actor;

public class ImmutableRoundRobinApp {

  public abstract static class Worker {
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

  public static void main(String[] args) throws IOException {
    Behavior<Void> root = Actor.deferred(ctx -> {
      ActorRef<Worker.Command> workerPool =
        ctx.spawn(ImmutableRoundRobin.roundRobinBehavior(3, Worker.behavior()), "workerPool");
      for (int n = 1; n <= 20; n++) {
        workerPool.tell(new Worker.Job(String.valueOf(n)));
      }

      return Actor.empty();
    });
    ActorSystem<Void> system = ActorSystem.create("RoundRobin", root);
    try {
      System.out.println("Press ENTER to exit the system");
      System.in.read();
    } finally {
      system.terminate();
    }
  }
}

