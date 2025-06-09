package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class Clique(val id: Identity[Clique])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[CliqueConnection]] = {
    val query = Clique.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def followers: Future[Seq[User]] = {
    val query = UserCliqueAssociation.table
      .filter(_.obj === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
    database.run(query)
  }

  def followersCount: Future[Int] = {
    val query = UserCliqueAssociation.table.filter(_.obj === id).length.result
    database.run(query)
  }

  def members: Future[Seq[User]] = {
    val query = UserCliqueAssociation.table
      .filter(_.obj === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
    database.run(query)
  }

  def membersCount: Future[Int] = {
    val query = UserCliqueAssociation.table.filter(_.obj === id).length.result
    database.run(query)
  }

  def posts: Future[Seq[Post]] = {
    val query = Post.table.filter(_.clique === id).sortBy(_.createdAt.desc).result
    database.run(query)
  }

  def postsCount: Future[Int] = {
    val query = Post.table.filter(_.clique === id).length.result
    database.run(query)
  }

  def achievements: Future[Seq[CliqueAchievement]] = {
    val query = CliqueAchievementAssociation.table
      .filter(_.subj === id)
      .join(CliqueAchievement.table)
      .on(_.obj === _.id)
      .map(_._2)
      .result
    database.run(query)
  }

  def achievementsCount: Future[Int] = {
    val query = CliqueAchievementAssociation.table.filter(_.subj === id).length.result
    database.run(query)
  }

  def updateName(name: String): Future[Int] = {
    val query = Clique.table.filter(_.id === id).map(_.name).update(name)
    database.run(query)
  }

  def updateBiography(biography: String): Future[Int] = {
    val query = Clique.table.filter(_.id === id).map(_.biography).update(biography)
    database.run(query)
  }
}

object Clique {
  lazy val table = TableQuery[CliqueTable]

  def create(name: String, biography: String)(implicit ec: ExecutionContext, database: Database): Future[Clique] = {
    val now = java.time.LocalDateTime.now()
    val query = table += CliqueConnection(name, biography, now)
    database.run(query).map(id => new Clique(id))
  }

  def findById(id: Identity[Clique])(implicit ec: ExecutionContext, database: Database): Future[Option[Clique]] = {
    val query = table.filter(_.id === id).result.headOption
    database.run(query).map(_.map(_ => new Clique(id)))
  }

  def findByName(name: String)(implicit ec: ExecutionContext, database: Database): Future[Option[Clique]] = {
    val query = table.filter(_.name === name).result.headOption
    database.run(query).map(_.map(conn => new Clique(conn.id)))
  }

  def delete(id: Identity[Clique])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(_.id === id).delete
    database.run(query)
  }
}

case class CliqueConnection(
                           id: Identity[Clique],
                           name: String,
                           biography: String,
                           createdAt: java.time.LocalDateTime
                         )

class CliqueTable(tag: Tag) extends Table[CliqueConnection](tag, "clique") {
  def id = column[Identity[Clique]]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.Unique)
  def biography = column[String]("biography")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  override def * : ProvenShape[CliqueConnection] = (id, name, biography, createdAt).mapTo[CliqueConnection]
} 