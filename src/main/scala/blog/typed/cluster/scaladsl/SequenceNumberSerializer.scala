package blog.typed.cluster.scaladsl

import akka.typed.cluster.ActorRefResolver
import akka.actor.ExtendedActorSystem
import akka.serialization.SerializerWithStringManifest
import java.nio.charset.StandardCharsets
import akka.typed.scaladsl.adapter._

/**
 * serialization-bindings for this is configured in cluster.conf
 */
class SequenceNumberSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {

  private val actorRefResolver = ActorRefResolver(system.toTyped)

  private val NextManifest = "a"

  override def identifier = 42

  override def manifest(msg: AnyRef) = msg match {
    case _: SequenceNumberGenerator.Next ⇒ NextManifest
  }

  override def toBinary(msg: AnyRef) = msg match {
    case SequenceNumberGenerator.Next(replyTo) ⇒
      ActorRefResolver(system.toTyped).toSerializationFormat(replyTo).getBytes(StandardCharsets.UTF_8)
  }
  override def fromBinary(bytes: Array[Byte], manifest: String) = {
    manifest match {
      case NextManifest ⇒
        val str = new String(bytes, StandardCharsets.UTF_8)
        val ref = actorRefResolver.resolveActorRef[Long](str)
        SequenceNumberGenerator.Next(ref)
    }
  }
}
