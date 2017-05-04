package blog.typed.javadsl;

import akka.actor.AbstractActor;
import akka.typed.ActorRef;
import akka.typed.Behavior;
import akka.typed.javadsl.Adapter;
import static akka.typed.javadsl.Actor.same;


public abstract class Coexistence1 {

  private Coexistence1() {
  }

  public static class MyUntyped1 extends AbstractActor {

    public static akka.actor.Props props() {
      return akka.actor.Props.create(MyUntyped1.class);
    }

    private final akka.typed.ActorRef<MyTyped1.Command> second =
        Adapter.spawn(getContext(), MyTyped1.behavior(), "second");

    @Override
    public void preStart() {
      Adapter.watch(getContext(), second);
      second.tell(new MyTyped1.Ping(Adapter.toTyped(getSelf())));
    }

    @Override
    public Receive createReceive() {
      return receiveBuilder()
        .match(MyTyped1.Pong.class, msg -> {
          System.out.println(getSelf() + " got Pong from " + getSender());
          Adapter.stop(getContext(), second);
        })
        .match(akka.actor.Terminated.class, t -> {
          System.out.println(getSelf() + " observed termination of " + t.getActor());
          getContext().stop(getSelf());
        })
        .build();
    }
  }

  public static abstract class MyTyped1 {
    private MyTyped1(){
    }

    interface Command {
    }

    public static class Ping implements Command {
      public final akka.typed.ActorRef<Pong> replyTo;

      public Ping(ActorRef<Pong> replyTo) {
        this.replyTo = replyTo;
      }
    }

    public static class Pong {
    }

    public static Behavior<Command> behavior() {
      return akka.typed.javadsl.Actor.immutable(MyTyped1.Command.class)
        .onMessage(MyTyped1.Ping.class, (ctx, msg) -> {
          System.out.println(ctx.getSelf() + " got Ping from " + msg.replyTo);
          msg.replyTo.tell(new Pong());
          return same();
        })
        .build();
    }

  }

}