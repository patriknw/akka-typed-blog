package blog.typed.cluster.scaladsl

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Actor

object SequenceNumberGenerator {

  sealed trait Message
  final case class Next(replyTo: ActorRef[Long]) extends Message
  case object Stop extends Message

  def generator(n: Long = System.currentTimeMillis()): Behavior[Message] = Actor.immutable {
    // a real generator would perhaps store the counter with Distributed Data to
    // be able to continue with next number after fail over
    case (_, Next(replyTo)) =>
      val next = n + 1
      replyTo ! next
      generator(next)
    case (_, Stop) =>
      Actor.stopped
  }

}
