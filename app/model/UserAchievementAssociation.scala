package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class UserAchievementAssociation(val id: Identity[UserAchievementAssociation])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[UserAchievementAssociationConnection]] = {
    val query = UserAchievementAssociation.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def user: Future[Option[User]] = {
    val query = UserAchievementAssociation.table
      .filter(_.id === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def achievement: Future[Option[UserAchievement]] = {
    val query = UserAchievementAssociation.table
      .filter(_.id === id)
      .join(UserAchievement.table)
      .on(_.obj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def markAsAchieved(achievedAt: java.time.LocalDateTime): Future[Int] = {
    val query = UserAchievementAssociation.table
      .filter(_.id === id)
      .map(_.achievedAt)
      .update(Some(achievedAt))
    database.run(query)
  }

  def isAchieved: Future[Boolean] = {
    val query = UserAchievementAssociation.table
      .filter(_.id === id)
      .map(_.achievedAt)
      .result
      .headOption
    database.run(query).map(_.isDefined)
  }
}

object UserAchievementAssociation {
  lazy val table = TableQuery[UserAchievementAssociationTable]

  def create(userId: Identity[User], achievementId: Identity[UserAchievement])(implicit ec: ExecutionContext, database: Database): Future[UserAchievementAssociation] = {
    val now = java.time.LocalDateTime.now()
    val query = table += UserAchievementAssociationConnection(userId, achievementId, None, now)
    database.run(query).map(_ => new UserAchievementAssociation(userId))
  }

  def findByUser(userId: Identity[User])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserAchievementAssociation]] = {
    val query = table.filter(_.subj === userId).result
    database.run(query).map(_.map(conn => new UserAchievementAssociation(conn.subj)))
  }

  def findByAchievement(achievementId: Identity[UserAchievement])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserAchievementAssociation]] = {
    val query = table.filter(_.obj === achievementId).result
    database.run(query).map(_.map(conn => new UserAchievementAssociation(conn.subj)))
  }

  def delete(userId: Identity[User], achievementId: Identity[UserAchievement])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(a => a.subj === userId && a.obj === achievementId).delete
    database.run(query)
  }
}

case class UserAchievementAssociationConnection(
                                              subj: Identity[User],
                                              obj: Identity[UserAchievement],
                                              achievedAt: Option[java.time.LocalDateTime],
                                              createdAt: java.time.LocalDateTime
                                            )

class UserAchievementAssociationTable(tag: Tag) extends Table[UserAchievementAssociationConnection](tag, "user_achievement_association") {
  def subj = column[Identity[User]]("subj")
  def obj = column[Identity[UserAchievement]]("obj")
  def achievedAt = column[Option[java.time.LocalDateTime]]("achieved_at")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  def pk = primaryKey("pk_user_achievement", (subj, obj))
  def subjFK = foreignKey("fk_user_achievement_subj", subj, User.table)(_.id)
  def objFK = foreignKey("fk_user_achievement_obj", obj, UserAchievement.table)(_.id)

  override def * : ProvenShape[UserAchievementAssociationConnection] = (subj, obj, achievedAt, createdAt).mapTo[UserAchievementAssociationConnection]
} 