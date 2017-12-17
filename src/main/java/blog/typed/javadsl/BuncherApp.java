package blog.typed.javadsl;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import scala.concurrent.duration.Duration;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Actor;

public class BuncherApp {

  public abstract static class BuncherDestination {
    private BuncherDestination() {
    }

    public static class Info implements Buncher.Msg {
      public final String s;

      public Info(String s) {
        this.s = s;
      }

      @Override
      public String toString() {
        return "Info(" + s + ")";
      }
    }

    public static Behavior<Buncher.Batch> behavior() {
      return Actor.immutable(Buncher.Batch.class)
        .onMessage(Buncher.Batch.class, (ctx, batch) -> {
          System.out.println("Got batch of " + batch.getMessages().size() + " messages: " + batch.getMessages());
          return Actor.same();
        })
        .build();
    }

  }

  public static void main(String[] args) throws IOException {
    Behavior<Void> root = Actor.deferred(ctx -> {

      ActorRef<Buncher.Batch> destination =
          ctx.spawn(BuncherDestination.behavior(), "destination");

      ActorRef<Buncher.Msg> buncher =
        ctx.spawn(Buncher.behavior(destination, Duration.create(3, SECONDS), 10), "buncher");

      for (int n = 1; n <= 15; n++) {
        buncher.tell(new BuncherDestination.Info(String.valueOf(n)));
      }

      ctx.schedule(Duration.create(1, SECONDS), buncher, new BuncherDestination.Info("16"));
      ctx.schedule(Duration.create(2, SECONDS), buncher, new BuncherDestination.Info("17"));

      ctx.schedule(Duration.create(4, SECONDS), buncher, new BuncherDestination.Info("18"));

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

