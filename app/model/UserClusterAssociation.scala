package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class UserClusterAssociation(val id: Identity[UserClusterAssociation])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[UserClusterAssociationConnection]] = {
    val query = UserClusterAssociation.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def user: Future[Option[User]] = {
    val query = UserClusterAssociation.table
      .filter(_.id === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def cluster: Future[Option[Cluster]] = {
    val query = UserClusterAssociation.table
      .filter(_.id === id)
      .join(Cluster.table)
      .on(_.obj === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def updateMemberRole(role: String): Future[Int] = {
    val query = UserClusterAssociation.table
      .filter(_.id === id)
      .map(_.memberRole)
      .update(Some(role))
    database.run(query)
  }

  def updateJoinDate(date: java.time.LocalDateTime): Future[Int] = {
    val query = UserClusterAssociation.table
      .filter(_.id === id)
      .map(_.joinDate)
      .update(Some(date))
    database.run(query)
  }
}

object UserClusterAssociation {
  lazy val table = TableQuery[UserClusterAssociationTable]

  def create(userId: Identity[User], clusterId: Identity[Cluster], role: Option[String] = None)(implicit ec: ExecutionContext, database: Database): Future[UserClusterAssociation] = {
    val now = java.time.LocalDateTime.now()
    val query = table += UserClusterAssociationConnection(userId, clusterId, role, Some(now), now)
    database.run(query).map(_ => new UserClusterAssociation(userId))
  }

  def findByUser(userId: Identity[User])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserClusterAssociation]] = {
    val query = table.filter(_.subj === userId).result
    database.run(query).map(_.map(conn => new UserClusterAssociation(conn.subj)))
  }

  def findByCluster(clusterId: Identity[Cluster])(implicit ec: ExecutionContext, database: Database): Future[Seq[UserClusterAssociation]] = {
    val query = table.filter(_.obj === clusterId).result
    database.run(query).map(_.map(conn => new UserClusterAssociation(conn.subj)))
  }

  def delete(userId: Identity[User], clusterId: Identity[Cluster])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(a => a.subj === userId && a.obj === clusterId).delete
    database.run(query)
  }
}

case class UserClusterAssociationConnection(
                                          subj: Identity[User],
                                          obj: Identity[Cluster],
                                          memberRole: Option[String],
                                          joinDate: Option[java.time.LocalDateTime],
                                          createdAt: java.time.LocalDateTime
                                        )

class UserClusterAssociationTable(tag: Tag) extends Table[UserClusterAssociationConnection](tag, "user_cluster_association") {
  def subj = column[Identity[User]]("subj")
  def obj = column[Identity[Cluster]]("obj")
  def memberRole = column[Option[String]]("member_role")
  def joinDate = column[Option[java.time.LocalDateTime]]("join_date")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  def pk = primaryKey("pk_user_cluster", (subj, obj))
  def subjFK = foreignKey("fk_user_cluster_subj", subj, User.table)(_.id)
  def objFK = foreignKey("fk_user_cluster_obj", obj, Cluster.table)(_.id)

  override def * : ProvenShape[UserClusterAssociationConnection] = (subj, obj, memberRole, joinDate, createdAt).mapTo[UserClusterAssociationConnection]
} 