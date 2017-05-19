package blog.typed.javadsl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;

import akka.typed.ActorRef;
import akka.typed.Behavior;
import akka.typed.PostStop;
import akka.typed.PreRestart;
import akka.typed.SupervisorStrategy;
import akka.typed.Terminated;
import akka.typed.javadsl.Actor;
import akka.typed.javadsl.ActorContext;

public abstract class FlakyWorkerManager {
  private FlakyWorkerManager() {
  }

  interface Command {
  }

  public static class Job implements Command {
    public final int partition;
    public final String payload;

    public Job(int partition, String payload) {
      this.partition = partition;
      this.payload = payload;
    }
  }

  private static class WorkerStopped implements Command {
    public final int partition;

    public WorkerStopped(int partition) {
      this.partition = partition;
    }
  }

  private static final SupervisorStrategy strategy =
      SupervisorStrategy.restartWithLimit(2, Duration.create(1, TimeUnit.SECONDS));
  private static final Behavior<FlakyWorker2.Command> worker =
      Actor.restarter(RuntimeException.class, strategy, FlakyWorker2.behavior());

  public static Behavior<Command> behavior() {
    return activeOption2(new HashMap<>());
  }

  private static ActorRef<FlakyWorker2.Command> spawnWorker(int partition,
      ActorContext<Command> ctx) {
    ActorRef<FlakyWorker2.Command> w = ctx.spawn(worker, "worker-" + partition);
    ctx.watchWith(w, new WorkerStopped(partition));
    return w;
  }

  //illustrate ordinary watch + Terminated
  private static Behavior<Command> activeOption1(Map<Integer, ActorRef<FlakyWorker2.Command>> workers) {
    return Actor.immutable(Command.class)
      .onMessage(Job.class, (ctx, msg) -> {
        ActorRef<FlakyWorker2.Command> w = workers.get(msg.partition);
        if (w == null) {
          w = spawnWorker(msg.partition, ctx);
          workers.put(msg.partition, w);
        }
        w.tell(new FlakyWorker2.Job(msg.payload));
        return activeOption1(workers);
      })
      .onSignal(Terminated.class, (ctx, sig) -> {
        ctx.getSystem().log().info("Worker {} is TERMINATED", sig.ref());
        int partition = -1;
        for (Map.Entry<Integer, ActorRef<FlakyWorker2.Command>> entry : workers.entrySet()) {
          if (entry.getValue().equals(sig.ref())) {
            partition = entry.getKey();
            break;
          }
        }
        workers.remove(partition);
        return activeOption1(workers);
      })
      .build();
  }

  //illustrate watchWith
  private static Behavior<Command> activeOption2(Map<Integer, ActorRef<FlakyWorker2.Command>> workers) {
    return Actor.immutable(Command.class)
      .onMessage(Job.class, (ctx, msg) -> {
        ActorRef<FlakyWorker2.Command> w = workers.get(msg.partition);
        if (w == null) {
          w = spawnWorker(msg.partition, ctx);
          workers.put(msg.partition, w);
        }
        w.tell(new FlakyWorker2.Job(msg.payload));
        return activeOption2(workers);
      })
      .onMessage(WorkerStopped.class, (ctx, msg) -> {
        ctx.getSystem().log().info("Worker {} is TERMINATED", workers.get(msg.partition));
        workers.remove(msg.partition);
        return activeOption1(workers);
      })
      .build();
  }

}