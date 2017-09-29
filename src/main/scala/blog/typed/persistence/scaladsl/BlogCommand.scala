package blog.typed.persistence.scaladsl

import akka.typed.ActorRef
import akka.Done

sealed trait BlogCommand {
  def postId: String
}

final case class AddPost(postId: String, content: PostContent, replyTo: ActorRef[AddPostDone]) extends BlogCommand

final case class AddPostDone(postId: String)

final case class GetPost(postId: String, replyTo: ActorRef[PostContent]) extends BlogCommand

final case class ChangeBody(postId: String, newBody: String, replyTo: ActorRef[Done]) extends BlogCommand

final case class Publish(postId: String, replyTo: ActorRef[Done]) extends BlogCommand

