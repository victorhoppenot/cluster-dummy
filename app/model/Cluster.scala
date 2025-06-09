package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class Cluster(val id: Identity[Cluster])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[ClusterConnection]] = {
    val query = Cluster.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def followers: Future[Seq[User]] = {
    val query = UserClusterAssociation.table
      .filter(_.obj === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
    database.run(query)
  }

  def followersCount: Future[Int] = {
    val query = UserClusterAssociation.table.filter(_.obj === id).length.result
    database.run(query)
  }

  def members: Future[Seq[User]] = {
    val query = UserClusterAssociation.table
      .filter(_.obj === id)
      .join(User.table)
      .on(_.subj === _.id)
      .map(_._2)
      .result
    database.run(query)
  }

  def membersCount: Future[Int] = {
    val query = UserClusterAssociation.table.filter(_.obj === id).length.result
    database.run(query)
  }

  def children: Future[Seq[Cluster]] = {
    val query = Cluster.table.filter(_.parentId.map(_.isLeft).getOrElse(false)).result
    database.run(query)
  }

  def childrenCount: Future[Int] = {
    val query = Cluster.table.filter(_.parentId.map(_.isLeft).getOrElse(false)).length.result
    database.run(query)
  }

  def parent: Future[Option[Either[Cluster, Community]]] = {
    val query = Cluster.table.filter(_.id === id).map(_.parentId).result.headOption
    database.run(query).map(_.map {
      case Left(clusterId) => Left(new Cluster(clusterId))
      case Right(communityId) => Right(new Community(communityId))
    })
  }

  def updateTitle(title: String): Future[Int] = {
    val query = Cluster.table.filter(_.id === id).map(_.title).update(title)
    database.run(query)
  }
}

object Cluster {
  lazy val table = TableQuery[ClusterTable]

  def create(title: String, parentId: Either[Identity[Cluster], Identity[Community]])(implicit ec: ExecutionContext, database: Database): Future[Cluster] = {
    val now = java.time.LocalDateTime.now()
    val query = table += ClusterConnection(title, parentId, now)
    database.run(query).map(id => new Cluster(id))
  }

  def findById(id: Identity[Cluster])(implicit ec: ExecutionContext, database: Database): Future[Option[Cluster]] = {
    val query = table.filter(_.id === id).result.headOption
    database.run(query).map(_.map(_ => new Cluster(id)))
  }

  def findByTitle(title: String)(implicit ec: ExecutionContext, database: Database): Future[Seq[Cluster]] = {
    val query = table.filter(_.title === title).result
    database.run(query).map(_.map(conn => new Cluster(conn.id)))
  }

  def findByParent(parentId: Either[Identity[Cluster], Identity[Community]])(implicit ec: ExecutionContext, database: Database): Future[Seq[Cluster]] = {
    val query = table.filter(_.parentId === parentId).result
    database.run(query).map(_.map(conn => new Cluster(conn.id)))
  }

  def delete(id: Identity[Cluster])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(_.id === id).delete
    database.run(query)
  }
}

case class ClusterConnection(
                           id: Identity[Cluster],
                           title: String,
                           parentId: Either[Identity[Cluster], Identity[Community]], // Can be either Community or Cluster
                           createdAt: java.time.LocalDateTime
                         )

class ClusterTable(tag: Tag) extends Table[ClusterConnection](tag, "cluster") {
  def id = column[Identity[Cluster]]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def parentId = column[Either[Identity[Cluster], Identity[Community]]]("parent_id")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  override def * : ProvenShape[ClusterConnection] = (id, title, parentId, createdAt).mapTo[ClusterConnection]
} 