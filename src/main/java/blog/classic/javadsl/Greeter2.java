package blog.classic.javadsl;

import akka.actor.AbstractActor;

public class Greeter2 extends AbstractActor {

  public static class Greet {
  }

  public static class WhoToGreet {
    public final String who;

    public WhoToGreet(String who) {
      this.who = who;
    }
  }


  @Override
  public Receive createReceive() {
    return onMessage("hello");
  }

  private Receive onMessage(String currentGreeting) {
    return receiveBuilder()
      .match(WhoToGreet.class, this::onWhoToGreet)
      .match(Greet.class, msg -> onGreet(currentGreeting))
      .build();
  }

  private void onWhoToGreet(WhoToGreet whoToGreet) {
    getContext().become(onMessage("hello, " + whoToGreet.who));
  }

  private void onGreet(String currentGreeting) {
    System.out.println(currentGreeting);
  }

}