package blog.typed.javadsl;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Signal;
import akka.actor.typed.javadsl.Actor;
import akka.actor.typed.javadsl.Actor.MutableBehavior;
import akka.actor.typed.javadsl.Actor.Receive;
import akka.actor.typed.javadsl.ActorContext;

public class MutableRoundRobin<T> extends MutableBehavior<T> {

  public static <T> Behavior<T> roundRobinBehavior(
      int numberOfWorkers, Behavior<T> worker) {
    return Actor.mutable(ctx -> new MutableRoundRobin<T>(ctx, numberOfWorkers, worker));
  }

  private final ActorRef<T>[] workers;

  private long index = 0;

  public MutableRoundRobin(ActorContext<T> ctx, int numberOfWorkers, Behavior<T> worker) {
    workers = new ActorRef[numberOfWorkers];
    for (int i = 0; i < numberOfWorkers; i++) {
      workers[i] = ctx.spawn(worker, "worker-" + (i+1));
    }
  }


  @Override
  public Receive<T> createReceive() {
    return new Actor.Receive<T>() {
      @Override
      public Behavior<T> receiveMessage(T msg) {
        return onMessage(msg);
      }

      @Override
      public Behavior<T> receiveSignal(Signal sig) {
        return Actor.unhandled();
      }
    };
  }

  private MutableRoundRobin<T> onMessage(T msg) {
    workers[(int) (index % workers.length)].tell(msg);
    index++;
    return this;
  }


}