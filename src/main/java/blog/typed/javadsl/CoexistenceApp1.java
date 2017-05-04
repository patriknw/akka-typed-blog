package blog.typed.javadsl;

import java.io.IOException;
import blog.typed.javadsl.Coexistence1.MyUntyped1;

public class CoexistenceApp1 {
  public static void main(String[] args) throws IOException {
    akka.actor.ActorSystem system = akka.actor.ActorSystem.create("sys");
    system.actorOf(MyUntyped1.props(), "first");
    try {
      System.out.println("Press ENTER to exit the system");
      System.in.read();
    } finally {
      system.terminate();
    }
  }
}

