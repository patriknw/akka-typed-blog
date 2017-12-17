package blog.typed.javadsl;

import java.io.IOException;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Actor;

public class FlakyWorkerApp2 {

  public static void main(String[] args) throws IOException {
    Behavior<Void> root = Actor.deferred(ctx -> {

      ActorRef<FlakyWorkerManager.Command> manager =
        ctx.spawn(FlakyWorkerManager.behavior(), "workerManager");

      int numberOfPartitions = 3;
      for (int n = 1; n <= 30; n++) {
        int partition = n % numberOfPartitions;
        manager.tell(new FlakyWorkerManager.Job(partition, String.valueOf(n)));
      }

      return Actor.empty();
    });
    ActorSystem<Void> system = ActorSystem.create(root, "Sys");
    try {
      System.out.println("Press ENTER to exit the system");
      System.in.read();
    } finally {
      system.terminate();
    }
  }

}

