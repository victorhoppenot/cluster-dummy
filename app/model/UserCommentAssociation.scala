package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class UserCommentAssociation(val id: Identity[UserCommentAssociation])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[UserCommentAssociationConnection]] = {
    val query = UserCommentAssociation.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def user: Future[Option[User]] = {
    val query = UserCommentAssociation.table
      .filter(_.id === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def comment: Future[Option[Comment]] = {
    val query = UserCommentAssociation.table
      .filter(_.id === id)
      .join(Comment.table)
      .on(_.obj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def toggleLike: Future[Int] = {
    val query = for {
      current <- UserCommentAssociation.table.filter(_.id === id).map(_.likes).result.headOption
      update <- UserCommentAssociation.table.filter(_.id === id).map(_.likes).update(!current.getOrElse(false))
    } yield update
    database.run(query)
  }

  def toggleView: Future[Int] = {
    val query = for {
      current <- UserCommentAssociation.table.filter(_.id === id).map(_.views).result.headOption
      update <- UserCommentAssociation.table.filter(_.id === id).map(_.views).update(!current.getOrElse(false))
    } yield update
    database.run(query)
  }

  def updateLastInteraction: Future[Int] = {
    val now = java.time.LocalDateTime.now()
    val query = UserCommentAssociation.table
      .filter(_.id === id)
      .map(_.lastInteractionDate)
      .update(Some(now))
    database.run(query)
  }
}

object UserCommentAssociation {
  lazy val table = TableQuery[UserCommentAssociationTable]

  def create(userId: Identity[User], commentId: Identity[Comment])(implicit ec: ExecutionContext, database: Database): Future[UserCommentAssociation] = {
    val now = java.time.LocalDateTime.now()
    val query = table += UserCommentAssociationConnection(userId, commentId, false, false, Some(now), now)
    database.run(query).map(_ => new UserCommentAssociation(userId))
  }

  def findByUser(userId: Identity[User])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserCommentAssociation]] = {
    val query = table.filter(_.subj === userId).result
    database.run(query).map(_.map(conn => new UserCommentAssociation(conn.subj)))
  }

  def findByComment(commentId: Identity[Comment])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserCommentAssociation]] = {
    val query = table.filter(_.obj === commentId).result
    database.run(query).map(_.map(conn => new UserCommentAssociation(conn.subj)))
  }

  def delete(userId: Identity[User], commentId: Identity[Comment])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(a => a.subj === userId && a.obj === commentId).delete
    database.run(query)
  }
}

case class UserCommentAssociationConnection(
                                          subj: Identity[User],
                                          obj: Identity[Comment],
                                          likes: Boolean,
                                          views: Boolean,
                                          lastInteractionDate: Option[java.time.LocalDateTime],
                                          createdAt: java.time.LocalDateTime
                                        )

class UserCommentAssociationTable(tag: Tag) extends Table[UserCommentAssociationConnection](tag, "user_comment_association") {
  def subj = column[Identity[User]]("subj")
  def obj = column[Identity[Comment]]("obj")
  def likes = column[Boolean]("likes")
  def views = column[Boolean]("views")
  def lastInteractionDate = column[Option[java.time.LocalDateTime]]("last_interaction_date")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  def pk = primaryKey("pk_user_comment", (subj, obj))
  def subjFK = foreignKey("fk_user_comment_subj", subj, User.table)(_.id)
  def objFK = foreignKey("fk_user_comment_obj", obj, Comment.table)(_.id)

  override def * : ProvenShape[UserCommentAssociationConnection] = (subj, obj, likes, views, lastInteractionDate, createdAt).mapTo[UserCommentAssociationConnection]
} 