package blog.typed.cluster.scaladsl

import java.util.concurrent.ThreadLocalRandom

import akka.actor.Address
import akka.cluster.ClusterEvent.ReachabilityEvent
import akka.cluster.ClusterEvent.ReachableMember
import akka.cluster.ClusterEvent.UnreachableMember
import akka.typed.ActorRef
import akka.typed.Behavior
import akka.typed.cluster.Cluster
import akka.typed.cluster.Subscribe
import akka.typed.receptionist.Receptionist
import akka.typed.receptionist.Receptionist.Listing
import akka.typed.receptionist.Receptionist.ServiceKey
import akka.typed.scaladsl.Actor

object RandomRouter {

  def router[T](serviceKey: ServiceKey[T]): Behavior[T] =
    Actor.deferred[Any] { ctx ⇒
      ctx.system.receptionist ! Receptionist.Subscribe(serviceKey, ctx.self)

      def routingBehavior(routees: Vector[ActorRef[T]]): Behavior[Any] =
        Actor.immutable { (ctx, msg) ⇒
          msg match {
            case Listing(_, services) ⇒
              routingBehavior(services.toVector)
            case other: T @unchecked ⇒
              if (routees.isEmpty)
                Actor.unhandled
              else {
                val i = ThreadLocalRandom.current.nextInt(routees.size)
                routees(i) ! other
                Actor.same
              }
          }
        }

      routingBehavior(Vector.empty)
    }.narrow[T]

  private final case class WrappedReachabilityEvent(event: ReachabilityEvent)

  // same as above, but also subscribes to cluster reachability events and
  // avoids routees that are unreachable
  def clusterRouter[T](serviceKey: ServiceKey[T]): Behavior[T] =
    Actor.deferred[Any] { ctx ⇒
      ctx.system.receptionist ! Receptionist.Subscribe(serviceKey, ctx.self)

      val cluster = Cluster(ctx.system)
      // typically you have to map such external messages into this
      // actor's protocol with a message adapter
      val reachabilityAdapter: ActorRef[ReachabilityEvent] = ctx.spawnAdapter(WrappedReachabilityEvent.apply)
      cluster.subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

      def routingBehavior(routees: Vector[ActorRef[T]], unreachable: Set[Address]): Behavior[Any] =
        Actor.immutable { (ctx, msg) ⇒
          msg match {
            case Listing(_, services) ⇒
              routingBehavior(services.toVector, unreachable)
            case WrappedReachabilityEvent(event) => event match {
              case UnreachableMember(m) =>
                routingBehavior(routees, unreachable + m.address)
              case ReachableMember(m) =>
                routingBehavior(routees, unreachable - m.address)
            }

            case other: T @unchecked ⇒
              if (routees.isEmpty)
                Actor.unhandled
              else {
                val reachableRoutes =
                  if (unreachable.isEmpty) routees
                  else routees.filterNot { r => unreachable(r.path.address) }

                val i = ThreadLocalRandom.current.nextInt(reachableRoutes.size)
                reachableRoutes(i) ! other
                Actor.same
              }
          }
        }

      routingBehavior(Vector.empty, Set.empty)
    }.narrow[T]

}
