package blog.typed.javadsl;

import akka.typed.Behavior;
import akka.typed.javadsl.Actor;

public abstract class Greeter2 {

  // no instances of this class, it's only a name space for messages
  // and static methods
  private Greeter2() {
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


  public static Behavior<Command> greeterBehavior() {
    return greeterBehavior("hello");
  }

  private static Behavior<Command> greeterBehavior(String currentGreeting) {
    return Actor.immutable(Greeter2.Command.class)
      .onMessage(Greeter2.WhoToGreet.class, (ctx, msg) -> greeterBehavior("hello, " + msg.who))
      .onMessage(Greeter2.Greet.class, (ctx, msg) -> {
        System.out.println(currentGreeting);
        return Actor.same();
      })
      .build();
  }

}