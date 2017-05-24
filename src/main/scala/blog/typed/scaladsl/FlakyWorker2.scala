package blog.typed.scaladsl

import java.util.concurrent.ThreadLocalRandom

import scala.concurrent.duration._
import scala.io.StdIn

import akka.typed.ActorSystem
import akka.typed.Behavior
import akka.typed.SupervisorStrategy
import akka.typed.scaladsl.Actor
import java.io.FileWriter
import java.io.Writer
import java.io.BufferedWriter
import java.io.PrintWriter
import akka.typed.PreRestart
import akka.typed.PostStop
import akka.typed.ActorRef
import akka.typed.scaladsl.ActorContext
import akka.typed.Terminated

object FlakyWorker2 {

  sealed trait Command
  final case class Job(payload: String) extends Command

  val workerBehavior: Behavior[Command] = Actor.deferred { ctx =>
    ctx.system.log.info("Worker {} is STARTED", ctx.self)
    val out = new PrintWriter(new FileWriter(
      s"target/out-${ctx.self.path.name}.txt", true))
    active(count = 1, out)
  }

  private def active(count: Int, out: PrintWriter): Behavior[Command] =
    Actor.immutable[Command] { (ctx, msg) =>
      msg match {
        case Job(payload) =>
          if (ThreadLocalRandom.current().nextInt(5) == 0)
            throw new RuntimeException("Bad luck")

          ctx.system.log.info("Worker {} got job {}, count {}", ctx.self, payload, count)
          out.println(s"Worker ${ctx.self} got job $payload, count $count")
          active(count + 1, out)
      }
    } onSignal {
      case (ctx, PreRestart) =>
        ctx.system.log.info("Worker {} is RESTARTED, count {}", ctx.self, count)
        out.close()
        Actor.same
      case (ctx, PostStop) =>
        ctx.system.log.info("Worker {} is STOPPED, count {}", ctx.self, count)
        out.close()
        Actor.same
    }
}

object WorkerManager {

  sealed trait Command
  final case class Job(partition: Int, payload: String) extends Command
  private final case class WorkerStopped(partition: Int) extends Command

  private val strategy = SupervisorStrategy.restartWithLimit(maxNrOfRetries = 2, 1.second)
  private val worker: Behavior[FlakyWorker2.Command] =
    Actor.supervise(FlakyWorker2.workerBehavior).onFailure[RuntimeException](strategy)

  val workerManagerBehavior: Behavior[Command] =
    activeOption2(Map.empty)

  private def spawnWorker(partition: Int, ctx: ActorContext[Command]): ActorRef[FlakyWorker2.Command] = {
    val w = ctx.spawn(worker, s"worker-$partition")
    ctx.watchWith(w, WorkerStopped(partition))
    w
  }

  // illustrate ordinary watch + Terminated
  private def activeOption1(workers: Map[Int, ActorRef[FlakyWorker2.Command]]): Behavior[Command] = {
    Actor.immutable[Command] { (ctx, msg) =>
      msg match {
        case job @ Job(partition, payload) =>
          val (w, newWorkers) = workers.get(partition) match {
            case Some(w) =>
              (w, workers)
            case None =>
              val w = spawnWorker(partition, ctx)
              (w, workers.updated(partition, w))
          }
          w ! FlakyWorker2.Job(payload)
          activeOption1(newWorkers)

        case _: WorkerStopped => Actor.same // not used in this option, but silence compiler warning
      }
    } onSignal {
      case (ctx, Terminated(ref)) =>
        ctx.system.log.info("Worker {} is TERMINATED", ref)
        val newWorkers = workers.filterNot { case (_, w) => w == ref }
        activeOption1(newWorkers)
    }
  }

  // illustrate watchWith
  private def activeOption2(workers: Map[Int, ActorRef[FlakyWorker2.Command]]): Behavior[Command] = {
    Actor.immutable[Command] { (ctx, msg) =>
      msg match {
        case job @ Job(partition, payload) =>
          val (w, newWorkers) = workers.get(partition) match {
            case Some(w) =>
              (w, workers)
            case None =>
              val w = spawnWorker(partition, ctx)
              (w, workers.updated(partition, w))
          }
          w ! FlakyWorker2.Job(payload)
          activeOption2(newWorkers)

        case WorkerStopped(partition) =>
          ctx.system.log.info("Worker {} is TERMINATED", workers(partition))
          activeOption2(workers - partition)
      }
    }
  }

}

object FlakyWorker2App {

  def main(args: Array[String]): Unit = {
    val root = Actor.deferred[Nothing] { ctx =>

      val manager = ctx.spawn(WorkerManager.workerManagerBehavior, "workerManager")

      val numberOfPartitions = 3
      (1 to 30).foreach { n =>
        val partition = n % numberOfPartitions
        manager ! WorkerManager.Job(partition, n.toString)
      }

      Actor.empty
    }
    val system = ActorSystem[Nothing]("Sys", root)
    try {
      // Exit the system after ENTER is pressed
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }

}
