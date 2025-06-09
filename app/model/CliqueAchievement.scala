package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class CliqueAchievement(val id: Identity[CliqueAchievement])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[CliqueAchievementConnection]] = {
    val query = CliqueAchievement.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def recipients: Future[Seq[Clique]] = {
    val query = CliqueAchievementAssociation.table
      .filter(_.obj === id)
      .join(Clique.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
    database.run(query)
  }

  def recipientsCount: Future[Int] = {
    val query = CliqueAchievementAssociation.table.filter(_.obj === id).length.result
    database.run(query)
  }

  def updateName(name: String): Future[Int] = {
    val query = CliqueAchievement.table.filter(_.id === id).map(_.name).update(name)
    database.run(query)
  }
}

object CliqueAchievement {
  lazy val table = TableQuery[CliqueAchievementTable]

  def create(name: String)(implicit ec: ExecutionContext, database: Database): Future[CliqueAchievement] = {
    val now = java.time.LocalDateTime.now()
    val query = table += CliqueAchievementConnection(name, now)
    database.run(query).map(id => new CliqueAchievement(id))
  }

  def findById(id: Identity[CliqueAchievement])(implicit ec: ExecutionContext, database: Database): Future[Option[CliqueAchievement]] = {
    val query = table.filter(_.id === id).result.headOption
    database.run(query).map(_.map(_ => new CliqueAchievement(id)))
  }

  def findByName(name: String)(implicit ec: ExecutionContext, database: Database): Future[Seq[CliqueAchievement]] = {
    val query = table.filter(_.name === name).result
    database.run(query).map(_.map(conn => new CliqueAchievement(conn.id)))
  }

  def delete(id: Identity[CliqueAchievement])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(_.id === id).delete
    database.run(query)
  }
}

case class CliqueAchievementConnection(
                                     id: Identity[CliqueAchievement],
                                     name: String,
                                     createdAt: java.time.LocalDateTime
                                   )

class CliqueAchievementTable(tag: Tag) extends Table[CliqueAchievementConnection](tag, "clique_achievement") {
  def id = column[Identity[CliqueAchievement]]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  override def * : ProvenShape[CliqueAchievementConnection] = (id, name, createdAt).mapTo[CliqueAchievementConnection]
} 