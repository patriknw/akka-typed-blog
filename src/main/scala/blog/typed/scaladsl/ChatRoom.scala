package blog.typed.scaladsl

import scala.io.StdIn

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Terminated

object ChatRoom {
  sealed trait Command
  final case class GetSession(screenName: String, replyTo: ActorRef[SessionEvent])
    extends Command
  private final case class PostSessionMessage(screenName: String, message: String)
    extends Command

  sealed trait SessionEvent
  final case class SessionGranted(handle: ActorRef[PostMessage]) extends SessionEvent
  final case class SessionDenied(reason: String) extends SessionEvent
  final case class MessagePosted(screenName: String, message: String) extends SessionEvent

  final case class PostMessage(message: String)

  val behavior: Behavior[Command] =
    chatRoom(List.empty)

  private def chatRoom(sessions: List[ActorRef[SessionEvent]]): Behavior[Command] =
    Actor.immutable[Command] { (ctx, msg) ⇒
      msg match {
        case GetSession(screenName, client) ⇒
          val wrapper = ctx.spawnAdapter {
            p: PostMessage ⇒ PostSessionMessage(screenName, p.message)
          }
          client ! SessionGranted(wrapper)
          chatRoom(client :: sessions)
        case PostSessionMessage(screenName, message) ⇒
          val mp = MessagePosted(screenName, message)
          sessions foreach (_ ! mp)
          Actor.same
      }
    }
}

object ChatRoomApp {

  import ChatRoom._

  val gabbler =
    Actor.immutable[SessionEvent] { (_, msg) ⇒
      msg match {
        case SessionDenied(reason) ⇒
          println(s"cannot start chat room session: $reason")
          Actor.stopped
        case SessionGranted(handle) ⇒
          handle ! PostMessage("Hello World!")
          Actor.same
        case MessagePosted(screenName, message) ⇒
          println(s"message has been posted by '$screenName': $message")
          Actor.stopped
      }
    }

  def main(args: Array[String]): Unit = {
    val root: Behavior[akka.NotUsed] =
      Actor.deferred { ctx ⇒
        val chatRoom = ctx.spawn(ChatRoom.behavior, "chatroom")
        val gabblerRef = ctx.spawn(gabbler, "gabbler")
        chatRoom ! GetSession("ol’ Gabbler", gabblerRef)

        Actor.empty
      }

    val system = ActorSystem(root, "ChatRoomDemo")
    try {
      // Exit the system after ENTER is pressed
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
