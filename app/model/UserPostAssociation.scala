package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class UserPostAssociation(val id: Identity[UserPostAssociation])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[UserPostAssociationConnection]] = {
    val query = UserPostAssociation.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def user: Future[Option[User]] = {
    val query = UserPostAssociation.table
      .filter(_.id === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def post: Future[Option[Post]] = {
    val query = UserPostAssociation.table
      .filter(_.id === id)
      .join(Post.table)
      .on(_.obj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def toggleLike: Future[Int] = {
    val query = for {
      current <- UserPostAssociation.table.filter(_.id === id).map(_.likes).result.headOption
      update <- UserPostAssociation.table.filter(_.id === id).map(_.likes).update(!current.getOrElse(false))
    } yield update
    database.run(query)
  }

  def toggleView: Future[Int] = {
    val query = for {
      current <- UserPostAssociation.table.filter(_.id === id).map(_.views).result.headOption
      update <- UserPostAssociation.table.filter(_.id === id).map(_.views).update(!current.getOrElse(false))
    } yield update
    database.run(query)
  }

  def updateLastInteraction: Future[Int] = {
    val now = java.time.LocalDateTime.now()
    val query = UserPostAssociation.table
      .filter(_.id === id)
      .map(_.lastInteractionDate)
      .update(Some(now))
    database.run(query)
  }
}

object UserPostAssociation {
  lazy val table = TableQuery[UserPostAssociationTable]

  def create(userId: Identity[User], postId: Identity[Post])(implicit ec: ExecutionContext, database: Database): Future[UserPostAssociation] = {
    val now = java.time.LocalDateTime.now()
    val query = table += UserPostAssociationConnection(userId, postId, false, false, Some(now), now)
    database.run(query).map(_ => new UserPostAssociation(userId))
  }

  def findByUser(userId: Identity[User])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserPostAssociation]] = {
    val query = table.filter(_.subj === userId).result
    database.run(query).map(_.map(conn => new UserPostAssociation(conn.subj)))
  }

  def findByPost(postId: Identity[Post])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserPostAssociation]] = {
    val query = table.filter(_.obj === postId).result
    database.run(query).map(_.map(conn => new UserPostAssociation(conn.subj)))
  }

  def delete(userId: Identity[User], postId: Identity[Post])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(a => a.subj === userId && a.obj === postId).delete
    database.run(query)
  }
}

case class UserPostAssociationConnection(
                                       subj: Identity[User],
                                       obj: Identity[Post],
                                       likes: Boolean,
                                       views: Boolean,
                                       lastInteractionDate: Option[java.time.LocalDateTime],
                                       createdAt: java.time.LocalDateTime
                                     )

class UserPostAssociationTable(tag: Tag) extends Table[UserPostAssociationConnection](tag, "user_post_association") {
  def subj = column[Identity[User]]("subj")
  def obj = column[Identity[Post]]("obj")
  def likes = column[Boolean]("likes")
  def views = column[Boolean]("views")
  def lastInteractionDate = column[Option[java.time.LocalDateTime]]("last_interaction_date")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  def pk = primaryKey("pk_user_post", (subj, obj))
  def subjFK = foreignKey("fk_user_post_subj", subj, User.table)(_.id)
  def objFK = foreignKey("fk_user_post_obj", obj, Post.table)(_.id)

  override def * : ProvenShape[UserPostAssociationConnection] = (subj, obj, likes, views, lastInteractionDate, createdAt).mapTo[UserPostAssociationConnection]
} 