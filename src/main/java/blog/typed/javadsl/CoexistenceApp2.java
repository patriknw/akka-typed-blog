package blog.typed.javadsl;

import java.io.IOException;
import blog.typed.javadsl.Coexistence2.MyTyped2;

import akka.actor.typed.javadsl.Adapter;

public class CoexistenceApp2 {
  public static void main(String[] args) throws IOException {
    akka.actor.ActorSystem system = akka.actor.ActorSystem.create("sys");
    Adapter.spawn(system, MyTyped2.behavior(), "first");
    try {
      System.out.println("Press ENTER to exit the system");
      System.in.read();
    } finally {
      system.terminate();
    }
  }
}

