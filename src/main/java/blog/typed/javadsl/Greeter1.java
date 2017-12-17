package blog.typed.javadsl;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Actor;
import akka.actor.typed.javadsl.Actor.MutableBehavior;
import akka.actor.typed.javadsl.Actor.Receive;

public class Greeter1 extends MutableBehavior<Greeter1.Command> {

  public static Behavior<Command> greeterBehavior() {
    return Actor.mutable(ctx -> new Greeter1());
  }

  interface Command {
  }

  public static class Greet implements Command {
  }

  public static class WhoToGreet implements Command {
    public final String who;

    public WhoToGreet(String who) {
      this.who = who;
    }
  }


  private String greeting = "hello";

  @Override
  public Receive<Command> createReceive() {
    return receiveBuilder()
      .onMessage(WhoToGreet.class, this::onWhoToGreet)
      .onMessage(Greet.class, this::onGreet)
      .build();
  }

  private Greeter1 onWhoToGreet(WhoToGreet whoToGreet) {
    greeting = "hello, " + whoToGreet.who;
    return this;
  }

  private Greeter1 onGreet(Greet greet) {
    System.out.println(greeting);
    return this;
  }

}