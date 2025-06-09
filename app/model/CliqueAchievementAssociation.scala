package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class CliqueAchievementAssociation(val id: Identity[CliqueAchievementAssociation])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[CliqueAchievementAssociationConnection]] = {
    val query = CliqueAchievementAssociation.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def clique: Future[Option[Clique]] = {
    val query = CliqueAchievementAssociation.table
      .filter(_.id === id)
      .join(Clique.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def achievement: Future[Option[CliqueAchievement]] = {
    val query = CliqueAchievementAssociation.table
      .filter(_.id === id)
      .join(CliqueAchievement.table)
      .on(_.obj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def markAsAchieved(achievedAt: java.time.LocalDateTime): Future[Int] = {
    val query = CliqueAchievementAssociation.table
      .filter(_.id === id)
      .map(_.achievedAt)
      .update(Some(achievedAt))
    database.run(query)
  }

  def isAchieved: Future[Boolean] = {
    val query = CliqueAchievementAssociation.table
      .filter(_.id === id)
      .map(_.achievedAt)
      .result
      .headOption
    database.run(query).map(_.isDefined)
  }
}

object CliqueAchievementAssociation {
  lazy val table = TableQuery[CliqueAchievementAssociationTable]

  def create(cliqueId: Identity[Clique], achievementId: Identity[CliqueAchievement])(implicit ec: ExecutionContext, database: Database): Future[CliqueAchievementAssociation] = {
    val now = java.time.LocalDateTime.now()
    val query = table += CliqueAchievementAssociationConnection(cliqueId, achievementId, None, now)
    database.run(query).map(_ => new CliqueAchievementAssociation(cliqueId))
  }

  def findByClique(cliqueId: Identity[Clique])(implicit ec: ExecutionContext, database: Database): Future[Seq[CliqueAchievementAssociation]] = {
    val query = table.filter(_.subj === cliqueId).result
    database.run(query).map(_.map(conn => new CliqueAchievementAssociation(conn.subj)))
  }

  def findByAchievement(achievementId: Identity[CliqueAchievement])(implicit ec: ExecutionContext, database: Database): Future[Seq[CliqueAchievementAssociation]] = {
    val query = table.filter(_.obj === achievementId).result
    database.run(query).map(_.map(conn => new CliqueAchievementAssociation(conn.subj)))
  }

  def delete(cliqueId: Identity[Clique], achievementId: Identity[CliqueAchievement])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(a => a.subj === cliqueId && a.obj === achievementId).delete
    database.run(query)
  }
}

case class CliqueAchievementAssociationConnection(
                                                subj: Identity[Clique],
                                                obj: Identity[CliqueAchievement],
                                                achievedAt: Option[java.time.LocalDateTime],
                                                createdAt: java.time.LocalDateTime
                                              )

class CliqueAchievementAssociationTable(tag: Tag) extends Table[CliqueAchievementAssociationConnection](tag, "clique_achievement_association") {
  def subj = column[Identity[Clique]]("subj")
  def obj = column[Identity[CliqueAchievement]]("obj")
  def achievedAt = column[Option[java.time.LocalDateTime]]("achieved_at")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  def pk = primaryKey("pk_clique_achievement", (subj, obj))
  def subjFK = foreignKey("fk_clique_achievement_subj", subj, Clique.table)(_.id)
  def objFK = foreignKey("fk_clique_achievement_obj", obj, CliqueAchievement.table)(_.id)

  override def * : ProvenShape[CliqueAchievementAssociationConnection] = (subj, obj, achievedAt, createdAt).mapTo[CliqueAchievementAssociationConnection]
} 