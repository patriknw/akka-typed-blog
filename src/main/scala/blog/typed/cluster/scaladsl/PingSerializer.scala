package blog.typed.cluster.scaladsl

import akka.typed.cluster.ActorRefResolver
import akka.actor.ExtendedActorSystem
import akka.serialization.SerializerWithStringManifest
import java.nio.charset.StandardCharsets
import akka.typed.scaladsl.adapter._

/**
 * serialization-bindings for this is configured in cluster.conf
 */
class PingSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {

  private val actorRefResolver = ActorRefResolver(system.toTyped)

  private val PingManifest = "a"
  private val PongManifest = "b"

  override def identifier = 41

  override def manifest(msg: AnyRef) = msg match {
    case _: Routee.Ping ⇒ PingManifest
    case Routee.Pong    ⇒ PongManifest
  }

  override def toBinary(msg: AnyRef) = msg match {
    case Routee.Ping(who) ⇒
      ActorRefResolver(system.toTyped).toSerializationFormat(who).getBytes(StandardCharsets.UTF_8)
    case Routee.Pong ⇒
      Array.emptyByteArray
  }
  override def fromBinary(bytes: Array[Byte], manifest: String) = {
    manifest match {
      case PingManifest ⇒
        val str = new String(bytes, StandardCharsets.UTF_8)
        val ref = actorRefResolver.resolveActorRef[Routee.Pong.type](str)
        Routee.Ping(ref)
      case PongManifest ⇒
        Routee.Pong
    }
  }
}
