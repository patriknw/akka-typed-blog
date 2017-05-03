package blog.typed.javadsl;

import akka.typed.ActorRef;
import akka.typed.Behavior;
import akka.typed.javadsl.Actor;

public abstract class ImmutableRoundRobin {
  private ImmutableRoundRobin() {
  }

  public static <T> Behavior<T> roundRobinBehavior(
      int numberOfWorkers, Behavior<T> worker) {
    return Actor.deferred(ctx -> {
      ActorRef<T>[] workers = new ActorRef[numberOfWorkers];
      for (int i = 0; i < numberOfWorkers; i++) {
        workers[i] = ctx.spawn(worker, "worker-" + (i+1));
      }
      return activeRoutingBehavior(0, workers);
    });
  }

  private static <T> Behavior<T> activeRoutingBehavior(long index, ActorRef<T>[] workers) {
    return Actor.immutable((ctx, msg) -> {
      workers[(int) (index % workers.length)].tell(msg);
      return activeRoutingBehavior(index + 1, workers);
    });
  }

}