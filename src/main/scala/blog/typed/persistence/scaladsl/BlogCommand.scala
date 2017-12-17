package blog.typed.persistence.scaladsl

import akka.actor.typed.ActorRef
import akka.Done
import java.io.Serializable

sealed trait BlogCommand extends Serializable

final case class AddPost(content: PostContent, replyTo: ActorRef[AddPostDone]) extends BlogCommand

final case class AddPostDone(postId: String)

final case class GetPost(replyTo: ActorRef[PostContent]) extends BlogCommand

final case class ChangeBody(newBody: String, replyTo: ActorRef[Done]) extends BlogCommand

final case class Publish(replyTo: ActorRef[Done]) extends BlogCommand

final case object PassivatePost extends BlogCommand

