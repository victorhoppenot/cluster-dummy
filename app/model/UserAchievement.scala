package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class UserAchievement(val id: Identity[UserAchievement])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[UserAchievementConnection]] = {
    val query = UserAchievement.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def recipients: Future[Seq[User]] = {
    val query = UserAchievementAssociation.table
      .filter(_.obj === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
    database.run(query)
  }

  def recipientsCount: Future[Int] = {
    val query = UserAchievementAssociation.table.filter(_.obj === id).length.result
    database.run(query)
  }

  def updateName(name: String): Future[Int] = {
    val query = UserAchievement.table.filter(_.id === id).map(_.name).update(name)
    database.run(query)
  }
}

object UserAchievement {
  lazy val table = TableQuery[UserAchievementTable]

  def create(name: String)(implicit ec: ExecutionContext, database: Database): Future[UserAchievement] = {
    val now = java.time.LocalDateTime.now()
    val query = table += UserAchievementConnection(name, now)
    database.run(query).map(id => new UserAchievement(id))
  }

  def findById(id: Identity[UserAchievement])(implicit ec: ExecutionContext, database: Database): Future[Option[UserAchievement]] = {
    val query = table.filter(_.id === id).result.headOption
    database.run(query).map(_.map(_ => new UserAchievement(id)))
  }

  def findByName(name: String)(implicit ec: ExecutionContext, database: Database): Future[Seq[UserAchievement]] = {
    val query = table.filter(_.name === name).result
    database.run(query).map(_.map(conn => new UserAchievement(conn.id)))
  }

  def delete(id: Identity[UserAchievement])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(_.id === id).delete
    database.run(query)
  }
}

case class UserAchievementConnection(
                                   id: Identity[UserAchievement],
                                   name: String,
                                   createdAt: java.time.LocalDateTime
                                 )

class UserAchievementTable(tag: Tag) extends Table[UserAchievementConnection](tag, "user_achievement") {
  def id = column[Identity[UserAchievement]]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  override def * : ProvenShape[UserAchievementConnection] = (id, name, createdAt).mapTo[UserAchievementConnection]
} 