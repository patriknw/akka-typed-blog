package blog.typed.persistence.scaladsl

object BlogState {
  val empty = BlogState(None, published = false)
}

final case class BlogState(
  content: Option[PostContent],
  published: Boolean) {

  def withContent(newContent: PostContent): BlogState =
    copy(content = Some(newContent))

  def isEmpty: Boolean = content.isEmpty

  def postId: String = content match {
    case Some(c) => c.postId
    case None    => throw new IllegalStateException("postId unknown before post is created")
  }
}

final case class PostContent(postId: String, title: String, body: String)

final case class PostSummary(postId: String, title: String)
