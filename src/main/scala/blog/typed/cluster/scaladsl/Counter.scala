package blog.typed.cluster.scaladsl

import akka.typed.scaladsl.adapter._
import akka.typed.ActorRef
import akka.typed.scaladsl.Actor
import akka.cluster.ddata.GCounter
import akka.typed.cluster.ddata.scaladsl.Replicator
import akka.typed.Behavior
import akka.cluster.ddata.ReplicatedData
import akka.cluster.ddata.GCounterKey
import akka.typed.cluster.ddata.scaladsl.DistributedData

object Counter {
  sealed trait ClientCommand
  final case object Increment extends ClientCommand
  final case class GetValue(replyTo: ActorRef[Int]) extends ClientCommand
  private sealed trait InternalMsg extends ClientCommand
  private case class InternalUpdateResponse[A <: ReplicatedData](rsp: Replicator.UpdateResponse[A]) extends InternalMsg
  private case class InternalGetResponse[A <: ReplicatedData](rsp: Replicator.GetResponse[A]) extends InternalMsg

  val Key = GCounterKey("counter")

  def behavior: Behavior[ClientCommand] =
    Actor.deferred[ClientCommand] { ctx ⇒
      // The ddata types still need the implicit untyped Cluster.
      // We will look into another solution for that.
      implicit val cluster = akka.cluster.Cluster(ctx.system.toUntyped)
      val replicator: ActorRef[Replicator.Command] = DistributedData(ctx.system).replicator

      val updateResponseAdapter: ActorRef[Replicator.UpdateResponse[GCounter]] =
        ctx.spawnAdapter(InternalUpdateResponse.apply)

      val getResponseAdapter: ActorRef[Replicator.GetResponse[GCounter]] =
        ctx.spawnAdapter(InternalGetResponse.apply)

      Actor.immutable[ClientCommand] { (ctx, msg) ⇒
        msg match {
          case Increment ⇒
            replicator ! Replicator.Update(Key, GCounter.empty, Replicator.WriteLocal, updateResponseAdapter)(_ + 1)
            Actor.same

          case GetValue(replyTo) ⇒
            replicator ! Replicator.Get(Key, Replicator.ReadLocal, getResponseAdapter, Some(replyTo))
            Actor.same

          case internal: InternalMsg ⇒ internal match {
            case InternalUpdateResponse(_) ⇒ Actor.same // ok

            case InternalGetResponse(rsp @ Replicator.GetSuccess(Key, Some(replyTo: ActorRef[Int] @unchecked))) ⇒
              val value = rsp.get(Key).value.toInt
              replyTo ! value
              Actor.same

            case InternalGetResponse(rsp) ⇒
              Actor.unhandled // not dealing with failures
          }
        }
      }
    }

}
