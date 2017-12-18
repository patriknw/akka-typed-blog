package blog.typed.persistence.scaladsl

import java.nio.charset.StandardCharsets
import akka.serialization.SerializerWithStringManifest
import akka.serialization.BaseSerializer
import blog.typed.persistence.scaladsl.protobuf.BlogPostMessages
import java.io.NotSerializableException
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.typed.ActorRefResolver
import akka.actor.typed.ActorRef

class BlogSerializer(val system: akka.actor.ExtendedActorSystem)
  extends SerializerWithStringManifest with BaseSerializer {

  private val resolver = ActorRefResolver(system.toTyped)

  private val BlogStateManifest = "aa"
  private val PostContentManifest = "ab"
  private val AddPostManifest = "ba"
  private val AddPostDoneManifest = "bb"
  private val ChangeBodyManifest = "bc"
  private val PublishManifest = "bd"
  private val PostAddedManifest = "ca"
  private val BodyChangedManifest = "cb"
  private val PublishedManifest = "cc"

  override def manifest(o: AnyRef): String = o match {
    case _: BlogState   ⇒ BlogStateManifest
    case _: PostContent ⇒ PostContentManifest
    case _: AddPost     ⇒ AddPostManifest
    case _: AddPostDone ⇒ AddPostDoneManifest
    case _: ChangeBody  ⇒ ChangeBodyManifest
    case _: Publish     ⇒ PublishManifest
    case _: PostAdded   ⇒ PostAddedManifest
    case _: BodyChanged ⇒ BodyChangedManifest
    case _: Published   ⇒ PublishedManifest
    case _ ⇒
      throw new IllegalArgumentException(s"Can't serialize object of type ${o.getClass} in [${getClass.getName}]")
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case a: BlogState   ⇒ blogStateToBinary(a)
    case a: PostContent ⇒ postContentToBinary(a)
    case a: AddPost     ⇒ addPostToBinary(a)
    case a: AddPostDone ⇒ addPostDoneToBinary(a)
    case a: ChangeBody  ⇒ changeBodyToBinary(a)
    case a: Publish     ⇒ publishToBinary(a)
    case a: PostAdded   ⇒ postAddedToBinary(a)
    case a: BodyChanged ⇒ bodyChangedToBinary(a)
    case a: Published   ⇒ publishedToBinary(a)

    case _ ⇒
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  private def blogStateToBinary(a: BlogState): Array[Byte] = {
    val builder = BlogPostMessages.BlogState.newBuilder()
    a.content match {
      case Some(c) => builder.setContent(postContentToProto(c))
      case None    => // no content
    }
    builder.setPublished(a.published)
    builder.build().toByteArray()
  }

  private def postContentToBinary(a: PostContent): Array[Byte] = {
    postContentToProto(a).build().toByteArray()
  }

  private def postContentToProto(a: PostContent): BlogPostMessages.PostContent.Builder = {
    val builder = BlogPostMessages.PostContent.newBuilder()
    builder.setPostId(a.postId).setTitle(a.title).setBody(a.body)
    builder
  }

  private def addPostToBinary(a: AddPost): Array[Byte] = {
    val builder = BlogPostMessages.AddPost.newBuilder()
    builder.setContent(postContentToProto(a.content))
    builder.setReplyTo(resolver.toSerializationFormat(a.replyTo))
    builder.build().toByteArray()
  }

  private def addPostDoneToBinary(a: AddPostDone): Array[Byte] = {
    val builder = BlogPostMessages.AddPostDone.newBuilder()
    builder.setPostId(a.postId)
    builder.build().toByteArray()
  }

  private def changeBodyToBinary(a: ChangeBody): Array[Byte] = {
    val builder = BlogPostMessages.ChangeBody.newBuilder()
    builder.setNewBody(a.newBody)
    builder.setReplyTo(resolver.toSerializationFormat(a.replyTo))
    builder.build().toByteArray()
  }

  private def publishToBinary(a: Publish): Array[Byte] = {
    val builder = BlogPostMessages.Publish.newBuilder()
    builder.setReplyTo(resolver.toSerializationFormat(a.replyTo))
    builder.build().toByteArray()
  }

  private def postAddedToBinary(a: PostAdded): Array[Byte] = {
    val builder = BlogPostMessages.PostAdded.newBuilder()
    builder.setPostId(a.postId).setContent(postContentToProto(a.content))
    builder.build().toByteArray()
  }

  private def bodyChangedToBinary(a: BodyChanged): Array[Byte] = {
    val builder = BlogPostMessages.BodyChanged.newBuilder()
    builder.setPostId(a.postId).setNewBody(a.newBody)
    builder.build().toByteArray()
  }

  private def publishedToBinary(a: Published): Array[Byte] = {
    val builder = BlogPostMessages.Published.newBuilder()
    builder.setPostId(a.postId)
    builder.build().toByteArray()
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case BlogStateManifest   ⇒ blogStateFromBinary(bytes)
    case PostContentManifest ⇒ postContentFromBinary(bytes)
    case AddPostManifest     ⇒ addPostFromBinary(bytes)
    case AddPostDoneManifest ⇒ addPostDoneFromBinary(bytes)
    case ChangeBodyManifest  ⇒ changeBodyFromBinary(bytes)
    case PublishManifest     ⇒ publishFromBinary(bytes)
    case PostAddedManifest   ⇒ postAddedFromBinary(bytes)
    case BodyChangedManifest ⇒ bodyChangedFromBinary(bytes)
    case PublishedManifest   ⇒ publishedFromBinary(bytes)

    case _ ⇒
      throw new NotSerializableException(
        s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
  }

  private def blogStateFromBinary(bytes: Array[Byte]): BlogState = {
    val a = BlogPostMessages.BlogState.parseFrom(bytes)
    val content =
      if (a.hasContent) {
        val c = a.getContent
        Some(PostContent(c.getPostId, c.getTitle, c.getBody))
      } else None

    BlogState(content, a.getPublished)
  }

  private def postContentFromBinary(bytes: Array[Byte]): PostContent = {
    val a = BlogPostMessages.PostContent.parseFrom(bytes)
    PostContent(a.getPostId, a.getTitle, a.getBody)
  }

  private def addPostFromBinary(bytes: Array[Byte]): AddPost = {
    val a = BlogPostMessages.AddPost.parseFrom(bytes)
    val c = a.getContent
    AddPost(
      PostContent(c.getPostId, c.getTitle, c.getBody),
      resolver.resolveActorRef(a.getReplyTo))
  }

  private def addPostDoneFromBinary(bytes: Array[Byte]): AddPostDone = {
    val a = BlogPostMessages.AddPostDone.parseFrom(bytes)
    AddPostDone(a.getPostId)
  }

  private def changeBodyFromBinary(bytes: Array[Byte]): ChangeBody = {
    val a = BlogPostMessages.ChangeBody.parseFrom(bytes)
    ChangeBody(a.getNewBody, resolver.resolveActorRef(a.getReplyTo))
  }

  private def publishFromBinary(bytes: Array[Byte]): Publish = {
    val a = BlogPostMessages.Publish.parseFrom(bytes)
    Publish(resolver.resolveActorRef(a.getReplyTo))
  }

  private def postAddedFromBinary(bytes: Array[Byte]): PostAdded = {
    val a = BlogPostMessages.PostAdded.parseFrom(bytes)
    val c = a.getContent
    PostAdded(a.getPostId, PostContent(c.getPostId, c.getTitle, c.getBody))
  }

  private def bodyChangedFromBinary(bytes: Array[Byte]): BodyChanged = {
    val a = BlogPostMessages.BodyChanged.parseFrom(bytes)
    BodyChanged(a.getPostId, a.getNewBody)
  }

  private def publishedFromBinary(bytes: Array[Byte]): Published = {
    val a = BlogPostMessages.Published.parseFrom(bytes)
    Published(a.getPostId)
  }

}
