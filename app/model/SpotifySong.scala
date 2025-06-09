package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class SpotifySong(val id: Identity[SpotifySong])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[SpotifySongConnection]] = {
    val query = SpotifySong.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def songs: Future[Seq[Song]] = {
    val query = Song.table.filter(_.spotify_id === id).result
    database.run(query)
  }

  def songsCount: Future[Int] = {
    val query = Song.table.filter(_.spotify_id === id).length.result
    database.run(query)
  }
}

object SpotifySong {
  lazy val table = TableQuery[SpotifySongTable]

  def create()(implicit ec: ExecutionContext, database: Database): Future[SpotifySong] = {
    val now = java.time.LocalDateTime.now()
    val query = table += SpotifySongConnection(now)
    database.run(query).map(id => new SpotifySong(id))
  }

  def findById(id: Identity[SpotifySong])(implicit ec: ExecutionContext, database: Database): Future[Option[SpotifySong]] = {
    val query = table.filter(_.id === id).result.headOption
    database.run(query).map(_.map(_ => new SpotifySong(id)))
  }

  def delete(id: Identity[SpotifySong])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(_.id === id).delete
    database.run(query)
  }
}

case class SpotifySongConnection(
                               id: Identity[SpotifySong],
                               foundAt: java.time.LocalDateTime
                             )

class SpotifySongTable(tag: Tag) extends Table[SpotifySongConnection](tag, "spotify_song") {
  def id = column[Identity[SpotifySong]]("id", O.PrimaryKey, O.AutoInc)
  def foundAt = column[java.time.LocalDateTime]("found_at")

  override def * : ProvenShape[SpotifySongConnection] = (id, foundAt).mapTo[SpotifySongConnection]
} 