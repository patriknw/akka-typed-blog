package blog.classic.javadsl;

  import java.io.IOException;

  import akka.actor.ActorRef;
  import akka.actor.Props;
  import akka.actor.ActorSystem;

  public class HelloWorldApp2 {
    public static void main(String[] args) throws IOException {
      ActorSystem system = ActorSystem.create("HelloWorld");
      try {
        ActorRef greeter = system.actorOf(Props.create(Greeter2.class), "greeter");
        greeter.tell(new Greeter2.WhoToGreet("World"), ActorRef.noSender());
        greeter.tell(new Greeter2.Greet(), ActorRef.noSender());

        System.out.println("Press ENTER to exit the system");
        System.in.read();
      } finally {
        system.terminate();
      }
    }
  }

