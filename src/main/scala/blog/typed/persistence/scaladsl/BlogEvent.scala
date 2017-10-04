package blog.typed.persistence.scaladsl

sealed trait BlogEvent extends Serializable

final case class PostAdded(
  postId: String,
  content: PostContent) extends BlogEvent

final case class BodyChanged(
  postId: String,
  newBody: String) extends BlogEvent

final case class Published(postId: String) extends BlogEvent

