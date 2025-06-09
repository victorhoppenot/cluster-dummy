package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class UserCliqueAssociation(val id: Identity[UserCliqueAssociation])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[UserCliqueAssociationConnection]] = {
    val query = UserCliqueAssociation.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def user: Future[Option[User]] = {
    val query = UserCliqueAssociation.table
      .filter(_.id === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def clique: Future[Option[Clique]] = {
    val query = UserCliqueAssociation.table
      .filter(_.id === id)
      .join(Clique.table)
      .on(_.obj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def updateMemberRole(role: String): Future[Int] = {
    val query = UserCliqueAssociation.table
      .filter(_.id === id)
      .map(_.memberRole)
      .update(Some(role))
    database.run(query)
  }

  def updateJoinDate(date: java.time.LocalDateTime): Future[Int] = {
    val query = UserCliqueAssociation.table
      .filter(_.id === id)
      .map(_.joinDate)
      .update(Some(date))
    database.run(query)
  }
}

object UserCliqueAssociation {
  lazy val table = TableQuery[UserCliqueAssociationTable]

  def create(userId: Identity[User], cliqueId: Identity[Clique], role: Option[String] = None)(implicit ec: ExecutionContext, database: Database): Future[UserCliqueAssociation] = {
    val now = java.time.LocalDateTime.now()
    val query = table += UserCliqueAssociationConnection(userId, cliqueId, role, Some(now), now)
    database.run(query).map(_ => new UserCliqueAssociation(userId))
  }

  def findByUser(userId: Identity[User])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserCliqueAssociation]] = {
    val query = table.filter(_.subj === userId).result
    database.run(query).map(_.map(conn => new UserCliqueAssociation(conn.subj)))
  }

  def findByClique(cliqueId: Identity[Clique])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserCliqueAssociation]] = {
    val query = table.filter(_.obj === cliqueId).result
    database.run(query).map(_.map(conn => new UserCliqueAssociation(conn.subj)))
  }

  def delete(userId: Identity[User], cliqueId: Identity[Clique])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(a => a.subj === userId && a.obj === cliqueId).delete
    database.run(query)
  }
}

case class UserCliqueAssociationConnection(
                                         subj: Identity[User],
                                         obj: Identity[Clique],
                                         memberRole: Option[String],
                                         joinDate: Option[java.time.LocalDateTime],
                                         createdAt: java.time.LocalDateTime
                                       )

class UserCliqueAssociationTable(tag: Tag) extends Table[UserCliqueAssociationConnection](tag, "user_clique_association") {
  def subj = column[Identity[User]]("subj")
  def obj = column[Identity[Clique]]("obj")
  def memberRole = column[Option[String]]("member_role")
  def joinDate = column[Option[java.time.LocalDateTime]]("join_date")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  def pk = primaryKey("pk_user_clique", (subj, obj))
  def subjFK = foreignKey("fk_user_clique_subj", subj, User.table)(_.id)
  def objFK = foreignKey("fk_user_clique_obj", obj, Clique.table)(_.id)

  override def * : ProvenShape[UserCliqueAssociationConnection] = (subj, obj, memberRole, joinDate, createdAt).mapTo[UserCliqueAssociationConnection]
}