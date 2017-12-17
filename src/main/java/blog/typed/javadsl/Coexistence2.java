package blog.typed.javadsl;

import akka.actor.AbstractActor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Adapter;
import static akka.actor.typed.javadsl.Actor.same;
import static akka.actor.typed.javadsl.Actor.stopped;


public abstract class Coexistence2 {

  private Coexistence2() {
  }

  public static abstract class MyTyped2 {
    private MyTyped2(){
    }

    public static class Ping {
      public final akka.actor.typed.ActorRef<Pong> replyTo;

      public Ping(ActorRef<Pong> replyTo) {
        this.replyTo = replyTo;
      }
    }

    interface Command {
    }

    public static class Pong implements Command {
    }

    public static Behavior<Command> behavior() {
      return akka.actor.typed.javadsl.Actor.deferred(context -> {
        akka.actor.ActorRef second =
          Adapter.actorOf(context, MyUntyped2.props(), "second");

        Adapter.watch(context, second);

        second.tell(new MyTyped2.Ping(context.getSelf().narrow()),
          Adapter.toUntyped(context.getSelf()));

        return akka.actor.typed.javadsl.Actor.immutable(MyTyped2.Command.class)
          .onMessage(MyTyped2.Pong.class, (ctx, msg) -> {
            // it's not possible to get the sender, that must be sent in message
            System.out.println(ctx.getSelf() + " got Pong");
            Adapter.stop(ctx, second);
            return same();
          })
          .onSignal(akka.actor.typed.Terminated.class, (ctx, sig) -> {
            System.out.println(ctx.getSelf() + " observed termination of " + sig.ref());
            return stopped();
          })
          .build();
      });
    }

  }

  public static class MyUntyped2 extends AbstractActor {

    public static akka.actor.Props props() {
      return akka.actor.Props.create(MyUntyped2.class);
    }

    @Override
    public Receive createReceive() {
      return receiveBuilder()
        .match(MyTyped2.Ping.class, msg -> {
          System.out.println(getSelf() + " got Ping from " + getSender());
          msg.replyTo.tell(new MyTyped2.Pong());
        })
        .build();
    }
  }

}