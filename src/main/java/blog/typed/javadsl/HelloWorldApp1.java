package blog.typed.javadsl;

import java.io.IOException;

import akka.typed.Behavior;
import akka.typed.ActorRef;
import akka.typed.ActorSystem;
import akka.typed.javadsl.Actor;

public class HelloWorldApp1 {
  public static void main(String[] args) throws IOException {
    Behavior<Void> root = Actor.deferred(ctx -> {
      ActorRef<Greeter1.Command> greeter =
        ctx.spawn(Greeter1.greeterBehavior(), "greeter");
      greeter.tell(new Greeter1.WhoToGreet("World"));
      greeter.tell(new Greeter1.Greet());

      return Actor.empty();
    });
    ActorSystem<Void> system = ActorSystem.create(root, "HelloWorld");
    try {
      System.out.println("Press ENTER to exit the system");
      System.in.read();
    } finally {
      system.terminate();
    }
  }
}

