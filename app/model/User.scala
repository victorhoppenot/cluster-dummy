package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class User(val id: Identity[User])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[UserConnection]] = {
    val query = User.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def following(start: Int, count: Int): Future[Seq[UserUserAssociationConnection]] = {
    val query = UserUserAssociation.table.filter(_.subj === id).sortBy(_.createdAt.desc).drop(start).take(count).result
    database.run(query)
  }

  def followingCount: Future[Int] = {
    val query = UserUserAssociation.table.filter(_.subj === id).length.result
    database.run(query)
  }

  def followers(start: Int, count: Int): Future[Seq[UserUserAssociationConnection]] = {
    val query = UserUserAssociation.table.filter(_.obj === id).sortBy(_.createdAt.desc).drop(start).take(count).result
    database.run(query)
  }

  def followersCount: Future[Int] = {
    val query = UserUserAssociation.table.filter(_.obj === id).length.result
    database.run(query)
  }

  def likedPosts(start: Int, count: Int): Future[Seq[UserPostAssociationConnection]] = {
    val query = UserPostAssociation.table.filter(_.subj === id).sortBy(_.createdAt.desc).drop(start).take(count).result
    database.run(query)
  }

  def likedPostsCount: Future[Int] = {
    val query = UserPostAssociation.table.filter(_.subj === id).length.result
    database.run(query)
  }

  def viewedPosts(start: Int, count: Int): Future[Seq[UserPostAssociationConnection]] = {
    val query = UserPostAssociation.table.filter(_.subj === id).sortBy(_.createdAt.desc).drop(start).take(count).result
    database.run(query)
  }

  def viewedPostsCount: Future[Int] = {
    val query = UserPostAssociation.table.filter(_.subj === id).length.result
    database.run(query)
  }
  
  def comments(start: Int, count: Int): Future[Seq[CommentConnection]] = {
    val query = Comment.table.filter(_.author === id).sortBy(_.createdAt.desc).drop(start).take(count).result
    database.run(query)
  }

  def commentsCount: Future[Int] = {
    val query = Comment.table.filter(_.author === id).length.result
    database.run(query)
  }

  def likedComments(start: Int, count: Int): Future[Seq[UserCommentAssociationConnection]] = {
    val query = UserCommentAssociation.table.filter(_.subj === id).sortBy(_.createdAt.desc).drop(start).take(count).result
    database.run(query)
  }

  def likedCommentsCount: Future[Int] = {
    val query = UserCommentAssociation.table.filter(_.subj === id).length.result
    database.run(query)
  }

  def posts(start: Int, count: Int): Future[Seq[PostConnection]] = {
    val query = Post.table.filter(_.poster === id).sortBy(_.createdAt.desc).drop(start).take(count).result
    database.run(query)
  }

  def postsCount: Future[Int] = {
    val query = Post.table.filter(_.poster === id).length.result
    database.run(query)
  }

  def cliques(start: Int, count: Int): Future[Seq[UserCliqueAssociationConnection]] = {
    val query = UserCliqueAssociation.table.filter(_.subj === id).sortBy(_.createdAt.desc).drop(start).take(count).result
    database.run(query)
  }

  def cliqueCount: Future[Int] = {
    val query = UserCliqueAssociation.table.filter(_.subj === id).length.result
    database.run(query)
  }

  def clusters(start: Int, count: Int): Future[Seq[UserClusterAssociationConnection]] = {
    val query = UserClusterAssociation.table.filter(_.subj === id).sortBy(_.createdAt.desc).drop(start).take(count).result
    database.run(query)
  }

  def clusterCount: Future[Int] = {
    val query = UserClusterAssociation.table.filter(_.subj === id).length.result
    database.run(query)
  }

  def achievementsCount: Future[Int]
}
object User {
  lazy val table = TableQuery[UserTable]
}

case class UserConnection(
                         id: Identity[User],
                         username: String,
                         hashedKey: String,
                         biography: String,
                         community: Identity[Community],
                         createdAt: java.time.LocalDateTime
                         )

class UserTable(tag: Tag) extends Table[UserConnection](tag, "user"){
  def id = column[Identity[User]]("id", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username", O.Unique)
  def hashedKey = column[String]("hashedKey")
  def biography = column[String]("biography")
  def community = column[Identity[Community]]("community_id")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  override def * : ProvenShape[UserConnection] = (id, username, hashedKey, biography, community, createdAt).mapTo[UserConnection]
}

