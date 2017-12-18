package blog.typed.javadsl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Actor;

public class FlakyWorkerApp {

  public static void main(String[] args) throws IOException {
    Behavior<Void> root = Actor.deferred(ctx -> {
      SupervisorStrategy strategy = SupervisorStrategy.restart();
      ActorRef<FlakyWorker.Command> worker =
        ctx.spawn(Actor.supervise(FlakyWorker.behavior())
          .onFailure(RuntimeException.class, strategy), "worker");

      for (int n = 1; n <= 20; n++) {
        worker.tell(new FlakyWorker.Job(String.valueOf(n)));
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

  private static void demonstrateOtherStrategies() {
    SupervisorStrategy strategy2 =
      SupervisorStrategy.restart().withLoggingEnabled(false);
    SupervisorStrategy strategy3 =
      SupervisorStrategy.resume();
    SupervisorStrategy strategy4 =
      SupervisorStrategy.restartWithLimit(3, Duration.create(1, TimeUnit.SECONDS));

    SupervisorStrategy backoff =
      SupervisorStrategy.restartWithBackoff(
        Duration.create(200, TimeUnit.MILLISECONDS),
        Duration.create(10, TimeUnit.SECONDS),
        0.1);
  }

  private static void demonstrateNesting() {
    SupervisorStrategy restart = SupervisorStrategy.restart();
    SupervisorStrategy limitedRestart =
      SupervisorStrategy.restartWithLimit(3, Duration.create(1, TimeUnit.SECONDS));

    Behavior<FlakyWorker.Command> behv =
      Actor.supervise(
        Actor.supervise(FlakyWorker.behavior()).onFailure(IllegalStateException.class, limitedRestart)
      ).onFailure(RuntimeException.class, restart);

  }
}

