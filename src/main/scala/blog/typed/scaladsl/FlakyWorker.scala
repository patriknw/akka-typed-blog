package blog.typed.scaladsl

import java.util.concurrent.ThreadLocalRandom

import scala.concurrent.duration._
import scala.io.StdIn

import akka.typed.ActorSystem
import akka.typed.Behavior
import akka.typed.SupervisorStrategy
import akka.typed.scaladsl.Actor

object FlakyWorker {
  sealed trait Command
  final case class Job(payload: String) extends Command

  val workerBehavior: Behavior[Command] =
    active(count = 1)

  private def active(count: Int): Behavior[Command] =
    Actor.immutable[Command] { (ctx, msg) =>
      msg match {
        case Job(payload) =>
          if (ThreadLocalRandom.current().nextInt(5) == 0)
            throw new RuntimeException("Bad luck")

          ctx.system.log.info("Worker {} got job {}, count {}", ctx.self, payload, count)
          active(count + 1)
      }
    }
}

object FlakyWorkerApp {

  def main(args: Array[String]): Unit = {
    val root = Actor.deferred[Nothing] { ctx =>
      import FlakyWorker._

      val strategy = SupervisorStrategy.restart
      val worker = ctx.spawn(
        Actor.supervise(workerBehavior).onFailure[RuntimeException](strategy),
        "worker")

      (1 to 20).foreach { n =>
        worker ! Job(n.toString)
      }

      Actor.empty
    }
    val system = ActorSystem[Nothing](root, "Sys")
    try {
      // Exit the system after ENTER is pressed
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }

  private def demonstrateOtherStrategies(): Unit = {
    val strategy2 = SupervisorStrategy.restart.withLoggingEnabled(false)
    val strategy3 = SupervisorStrategy.resume
    val strategy4 = SupervisorStrategy.restartWithLimit(maxNrOfRetries = 3, 1.second)

    val backoff = SupervisorStrategy.restartWithBackoff(
      minBackoff = 200.millis, maxBackoff = 10.seconds, randomFactor = 0.1)
  }

  private def demonstrateNesting(): Unit = {
    import FlakyWorker._
    import Actor.supervise
    import SupervisorStrategy._
    val behv: Behavior[Command] =
      supervise(
        supervise(workerBehavior).onFailure[IllegalStateException](restartWithLimit(3, 1.second)))
        .onFailure[RuntimeException](restart)
  }

}
