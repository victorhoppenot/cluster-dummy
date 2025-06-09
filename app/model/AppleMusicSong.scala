package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class AppleMusicSong(val id: Identity[AppleMusicSong])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[AppleMusicSongConnection]] = {
    val query = AppleMusicSong.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def songs: Future[Seq[Song]] = {
    val query = Song.table.filter(_.apple_music_id === id).result
    database.run(query)
  }

  def songsCount: Future[Int] = {
    val query = Song.table.filter(_.apple_music_id === id).length.result
    database.run(query)
  }
}

object AppleMusicSong {
  lazy val table = TableQuery[AppleMusicSongTable]

  def create()(implicit ec: ExecutionContext, database: Database): Future[AppleMusicSong] = {
    val now = java.time.LocalDateTime.now()
    val query = table += AppleMusicSongConnection(now)
    database.run(query).map(id => new AppleMusicSong(id))
  }

  def findById(id: Identity[AppleMusicSong])(implicit ec: ExecutionContext, database: Database): Future[Option[AppleMusicSong]] = {
    val query = table.filter(_.id === id).result.headOption
    database.run(query).map(_.map(_ => new AppleMusicSong(id)))
  }

  def delete(id: Identity[AppleMusicSong])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(_.id === id).delete
    database.run(query)
  }
}

case class AppleMusicSongConnection(
                                  id: Identity[AppleMusicSong],
                                  foundAt: java.time.LocalDateTime
                                )

class AppleMusicSongTable(tag: Tag) extends Table[AppleMusicSongConnection](tag, "apple_music_song") {
  def id = column[Identity[AppleMusicSong]]("id", O.PrimaryKey, O.AutoInc)
  def foundAt = column[java.time.LocalDateTime]("found_at")

  override def * : ProvenShape[AppleMusicSongConnection] = (id, foundAt).mapTo[AppleMusicSongConnection]
} 