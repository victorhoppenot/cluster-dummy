package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class Song(val id: Identity[Song])(implicit ec: ExecutionContext, database: Database) {

    def connection: Future[Option[SongConnection]] = {
        val query = Song.table.filter(_.id === id).result.headOption
        database.run(query)
    }

    def spotifySong: Future[Option[SpotifySong]] = {
        val query = Song.table
            .filter(_.id === id)
            .join(SpotifySong.table)
            .on(_.spotify_id === _.id)
            .map(_._2)
            .result
            .headOption
        database.run(query)
    }

    def appleMusicSong: Future[Option[AppleMusicSong]] = {
        val query = Song.table
            .filter(_.id === id)
            .join(AppleMusicSong.table)
            .on(_.apple_music_id === _.id)
            .map(_._2)
            .result
            .headOption
        database.run(query)
    }

    def posts: Future[Seq[Post]] = {
        val query = Post.table.filter(_.song === id).result
        database.run(query)
    }

    def postsCount: Future[Int] = {
        val query = Post.table.filter(_.song === id).length.result
        database.run(query)
    }

    def updateTitle(title: String): Future[Int] = {
        val query = Song.table.filter(_.id === id).map(_.title).update(title)
        database.run(query)
    }

    def clusters(start: Int, count: Int): Future[Seq[Identity[Cluster]]]

    def clusterCount: Future[Int]
}

object Song {
    lazy val table = TableQuery[SongTable]

    def create(title: String, spotifyId: Identity[SpotifySong], appleMusicId: Identity[AppleMusicSong])(implicit ec: ExecutionContext, database: Database): Future[Song] = {
        val now = java.time.LocalDateTime.now()
        val query = table += SongConnection(title, spotifyId, appleMusicId, now)
        database.run(query).map(id => new Song(id))
    }

    def findById(id: Identity[Song])(implicit ec: ExecutionContext, database: Database): Future[Option[Song]] = {
        val query = table.filter(_.id === id).result.headOption
        database.run(query).map(_.map(_ => new Song(id)))
    }

    def findByTitle(title: String)(implicit ec: ExecutionContext, database: Database): Future[Seq[Song]] = {
        val query = table.filter(_.title === title).result
        database.run(query).map(_.map(conn => new Song(conn.id)))
    }

    def findBySpotifyId(spotifyId: Identity[SpotifySong])(implicit ec: ExecutionContext, database: Database): Future[Option[Song]] = {
        val query = table.filter(_.spotify_id === spotifyId).result.headOption
        database.run(query).map(_.map(conn => new Song(conn.id)))
    }

    def findByAppleMusicId(appleMusicId: Identity[AppleMusicSong])(implicit ec: ExecutionContext, database: Database): Future[Option[Song]] = {
        val query = table.filter(_.apple_music_id === appleMusicId).result.headOption
        database.run(query).map(_.map(conn => new Song(conn.id)))
    }

    def delete(id: Identity[Song])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
        val query = table.filter(_.id === id).delete
        database.run(query)
    }
}

case class SongConnection(
    id: Identity[Song],
    title: String,
    spotify_id: Identity[SpotifySong],
    apple_music_id: Identity[AppleMusicSong],
    createdAt: java.time.LocalDateTime
)

class SongTable(tag: Tag) extends Table[SongConnection](tag, "song") {
    def id = column[Identity[Song]]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def spotify_id = column[Identity[SpotifySong]]("spotify_id", O.Unique)
    def apple_music_id = column[Identity[AppleMusicSong]]("apple_music_id", O.Unique)
    def createdAt = column[java.time.LocalDateTime]("created_at")

    def spotifyFK = foreignKey("fk_song_spotify", spotify_id, SpotifySong.table)(_.id)
    def appleMusicFK = foreignKey("fk_song_apple_music", apple_music_id, AppleMusicSong.table)(_.id)

    override def * : ProvenShape[SongConnection] = (id, title, spotify_id, apple_music_id, createdAt).mapTo[SongConnection]
}
