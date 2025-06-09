package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class Comment(val id: Identity[Comment])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[CommentConnection]] = {
    val query = Comment.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def author: Future[Option[User]] = {
    val query = Comment.table
      .filter(_.id === id)
      .join(User.table)
      .on(_.author === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def parentComment: Future[Option[Comment]] = {
    val query = Comment.table
      .filter(_.id === id)
      .join(Comment.table)
      .on(_.parent === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def likes: Future[Seq[UserCommentAssociationConnection]] = {
    val query = UserCommentAssociation.table.filter(a => a.obj === id && a.likes).result
    database.run(query)
  }

  def likeCount: Future[Int] = {
    val query = UserCommentAssociation.table.filter(a => a.obj === id && a.likes).length.result
    database.run(query)
  }

  def replies: Future[Seq[Comment]] = {
    val query = Comment.table.filter(_.parent === id).result
    database.run(query)
  }

  def replyCount: Future[Int] = {
    val query = Comment.table.filter(_.parent === id).length.result
    database.run(query)
  }

  def views: Future[Seq[UserCommentAssociationConnection]] = {
    val query = UserCommentAssociation.table.filter(a => a.obj === id && a.views).result
    database.run(query)
  }

  def viewCount: Future[Int] = {
    val query = UserCommentAssociation.table.filter(a => a.obj === id && a.views).length.result
    database.run(query)
  }

  def parent: Future[Option[Either[Identity[Comment], Identity[Post]]]] = {
    val query = Comment.table.filter(_.id === id).map(_.parent).result.headOption
    database.run(query)
  }

  def updateContent(content: String): Future[Int] = {
    val query = Comment.table.filter(_.id === id).map(_.content).update(content)
    database.run(query)
  }
}

object Comment {
  lazy val table = TableQuery[CommentTable]

  def create(content: String, authorId: Identity[User], parentId: Option[Identity[Comment]])(implicit ec: ExecutionContext, database: Database): Future[Comment] = {
    val now = java.time.LocalDateTime.now()
    val query = table += CommentConnection(content, authorId, parentId, now)
    database.run(query).map(id => new Comment(id))
  }

  def findById(id: Identity[Comment])(implicit ec: ExecutionContext, database: Database): Future[Option[Comment]] = {
    val query = table.filter(_.id === id).result.headOption
    database.run(query).map(_.map(_ => new Comment(id)))
  }

  def findByAuthor(authorId: Identity[User])(implicit ec: ExecutionContext, database: Database): Future[Seq[Comment]] = {
    val query = table.filter(_.author === authorId).result
    database.run(query).map(_.map(conn => new Comment(conn.id)))
  }

  def findByParent(parentId: Identity[Comment])(implicit ec: ExecutionContext, database: Database): Future[Seq[Comment]] = {
    val query = table.filter(_.parent === parentId).result
    database.run(query).map(_.map(conn => new Comment(conn.id)))
  }

  def delete(id: Identity[Comment])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(_.id === id).delete
    database.run(query)
  }
}

case class CommentConnection(
                           id: Identity[Comment],
                           content: String,
                           author: Identity[User],
                           parent: Option[Identity[Comment]],
                           createdAt: java.time.LocalDateTime
                         )

class CommentTable(tag: Tag) extends Table[CommentConnection](tag, "comment") {
  def id = column[Identity[Comment]]("id", O.PrimaryKey, O.AutoInc)
  def content = column[String]("content")
  def author = column[Identity[User]]("author_id")
  def parent = column[Identity[Comment]]("parent_id")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  def authorFK = foreignKey("fk_comment_author", author, User.table)(_.id)
  def parentFK = foreignKey("fk_comment_parent", parent, Comment.table)(_.id)

  override def * : ProvenShape[CommentConnection] = (id, content, author, parent, createdAt).mapTo[CommentConnection]
} 