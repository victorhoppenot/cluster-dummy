package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class Community(val id: Identity[Community])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[CommunityConnection]] = {
    val query = Community.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def members: Future[Seq[User]] = {
    val query = User.table.filter(_.community === id).result
    database.run(query)
  }

  def membersCount: Future[Int] = {
    val query = User.table.filter(_.community === id).length.result
    database.run(query)
  }

  def clusters: Future[Seq[Cluster]] = {
    val query = Cluster.table.filter(_.parentId.map(_.isRight).getOrElse(false)).result
    database.run(query)
  }

  def clusterCount: Future[Int] = {
    val query = Cluster.table.filter(_.parentId.map(_.isRight).getOrElse(false)).length.result
    database.run(query)
  }

  def updateTitle(title: String): Future[Int] = {
    val query = Community.table.filter(_.id === id).map(_.title).update(title)
    database.run(query)
  }

  def updateArea(area: String): Future[Int] = {
    val query = Community.table.filter(_.id === id).map(_.area_geojson).update(area)
    database.run(query)
  }
}

object Community {
  lazy val table = TableQuery[CommunityTable]

  def create(title: String, area: String)(implicit ec: ExecutionContext, database: Database): Future[Community] = {
    val now = java.time.LocalDateTime.now()
    val query = table += CommunityConnection(title, area, now)
    database.run(query).map(id => new Community(id))
  }

  def findById(id: Identity[Community])(implicit ec: ExecutionContext, database: Database): Future[Option[Community]] = {
    val query = table.filter(_.id === id).result.headOption
    database.run(query).map(_.map(_ => new Community(id)))
  }

  def findByTitle(title: String)(implicit ec: ExecutionContext, database: Database): Future[Seq[Community]] = {
    val query = table.filter(_.title === title).result
    database.run(query).map(_.map(conn => new Community(conn.id)))
  }

  def findByArea(area: String)(implicit ec: ExecutionContext, database: Database): Future[Seq[Community]] = {
    val query = table.filter(_.area_geojson === area).result
    database.run(query).map(_.map(conn => new Community(conn.id)))
  }

  def delete(id: Identity[Community])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(_.id === id).delete
    database.run(query)
  }
}

case class CommunityConnection(
                             id: Identity[Community],
                             title: String,
                             area_geojson: String,
                             createdAt: java.time.LocalDateTime
                           )

class CommunityTable(tag: Tag) extends Table[CommunityConnection](tag, "community") {
  def id = column[Identity[Community]]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def area_geojson = column[String]("area_geojson")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  override def * : ProvenShape[CommunityConnection] = (id, title, area_geojson, createdAt).mapTo[CommunityConnection]
} 