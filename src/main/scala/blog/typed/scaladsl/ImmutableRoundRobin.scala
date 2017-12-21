package blog.typed.scaladsl

import scala.io.StdIn

import akka.typed.ActorSystem
import akka.typed.Behavior
import akka.typed.scaladsl.Actor
import akka.typed.ActorRef

object ImmutableRoundRobin {

  def roundRobinBehavior[T](numberOfWorkers: Int, worker: Behavior[T]): Behavior[T] =
    Actor.deferred { ctx =>
      val workers = (1 to numberOfWorkers).map { n =>
        ctx.spawn(worker, s"worker-$n")
      }
      activeRoutingBehavior(index = 0, workers.toVector)
    }

  private def activeRoutingBehavior[T](index: Long, workers: Vector[ActorRef[T]]): Behavior[T] =
    Actor.immutable[T] { (ctx, msg) =>
      workers(abs(index % workers.size).toInt) ! msg
      activeRoutingBehavior(index + 1, workers)
    }
}

object ImmutableRoundRobinApp {

  def main(args: Array[String]): Unit = {
    val root = Actor.deferred[Nothing] { ctx =>
      import Worker._
      import ImmutableRoundRobin._
      val workerPool = ctx.spawn(roundRobinBehavior(numberOfWorkers = 3, workerBehavior), "workerPool")
      (1 to 20).foreach { n =>
        workerPool ! Job(n.toString)
      }

      Actor.empty
    }
    val system = ActorSystem[Nothing](root, "RoundRobin")
    try {
      // Exit the system after ENTER is pressed
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
