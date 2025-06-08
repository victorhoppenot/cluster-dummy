package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class Post(val id: Identity[Post])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[PostConnection]] = {
    val query = Post.table.filter(_.id === id).result.headOption
    database.run(query)
  }

  def caption: Future[Option[Comment]] = {
    val query = Post.table
      .filter(_.id === id)
      .join(Comment.table)
      .on(_.caption === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def song: Future[Option[Song]] = {
    val query = Post.table
      .filter(_.id === id)
      .join(Song.table)
      .on(_.song === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def poster: Future[Option[User]] = {
    val query = Post.table
      .filter(_.id === id)
      .join(User.table)
      .on(_.poster === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def clique: Future[Option[Clique]] = {
    val query = Post.table
      .filter(_.id === id)
      .join(Clique.table)
      .on(_.clique === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def cluster: Future[Option[Cluster]] = {
    val query = Post.table
      .filter(_.id === id)
      .join(Cluster.table)
      .on(_.cluster === _.id)
      .map(_._2)
      .result
      .headOption
    database.run(query)
  }

  def likes: Future[Seq[UserPostAssociationConnection]] = {
    val query = UserPostAssociation.table.filter(a => a.obj === id && a.likes).result
    database.run(query)
  }

  def likeCount: Future[Int] = {
    val query = UserPostAssociation.table.filter(a => a.obj === id && a.likes).length.result
    database.run(query)
  }

  def views: Future[Seq[UserPostAssociationConnection]] = {
    val query = UserPostAssociation.table.filter(a => a.obj === id && a.views).result
    database.run(query)
  }

  def viewCount: Future[Int] = {
    val query = UserPostAssociation.table.filter(a => a.obj === id && a.views).length.result
    database.run(query)
  }
}

object Post {
  lazy val table = TableQuery[PostTable]

  def create(captionId: Identity[Comment], songId: Identity[Song], posterId: Identity[User], cliqueId: Identity[Clique], clusterId: Identity[Cluster])(implicit ec: ExecutionContext, database: Database): Future[Post] = {
    val now = java.time.LocalDateTime.now()
    val query = table += PostConnection(captionId, songId, posterId, cliqueId, clusterId, now)
    database.run(query).map(id => new Post(id))
  }

  def findById(id: Identity[Post])(implicit ec: ExecutionContext, database: Database): Future[Option[Post]] = {
    val query = table.filter(_.id === id).result.headOption
    database.run(query).map(_.map(_ => new Post(id)))
  }

  def findByPoster(posterId: Identity[User])(implicit ec: ExecutionContext, database: Database): Future[Seq[Post]] = {
    val query = table.filter(_.poster === posterId).result
    database.run(query).map(_.map(conn => new Post(conn.id)))
  }

  def findByClique(cliqueId: Identity[Clique])(implicit ec: ExecutionContext, database: Database): Future[Seq[Post]] = {
    val query = table.filter(_.clique === cliqueId).result
    database.run(query).map(_.map(conn => new Post(conn.id)))
  }

  def findByCluster(clusterId: Identity[Cluster])(implicit ec: ExecutionContext, database: Database): Future[Seq[Post]] = {
    val query = table.filter(_.cluster === clusterId).result
    database.run(query).map(_.map(conn => new Post(conn.id)))
  }

  def findBySong(songId: Identity[Song])(implicit ec: ExecutionContext, database: Database): Future[Seq[Post]] = {
    val query = table.filter(_.song === songId).result
    database.run(query).map(_.map(conn => new Post(conn.id)))
  }

  def delete(id: Identity[Post])(implicit ec: ExecutionContext, database: Database): Future[Int] = {
    val query = table.filter(_.id === id).delete
    database.run(query)
  }
}

case class PostConnection(
                           id: Identity[Post],
                           caption: Identity[Comment],
                           song: Identity[Song],
                           poster: Identity[User],
                           clique: Identity[Clique],
                           cluster: Identity[Cluster],
                           createdAt: java.time.LocalDateTime
                         )

class PostTable(tag: Tag) extends Table[PostConnection](tag, "post") {
  def id = column[Identity[Post]]("id", O.PrimaryKey, O.AutoInc)
  def caption = column[Identity[Comment]]("caption_id", O.Unique)
  def song = column[Identity[Song]]("song_id")
  def poster = column[Identity[User]]("poster_id")
  def clique = column[Identity[Clique]]("clique_id")
  def cluster = column[Identity[Cluster]]("cluster_id")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  def captionFK = foreignKey("fk_post_caption", caption, Comment.table)(_.id)
  def songFK = foreignKey("fk_post_song", song, Song.table)(_.id)
  def posterFK = foreignKey("fk_post_poster", poster, User.table)(_.id)
  def cliqueFK = foreignKey("fk_post_clique", clique, Clique.table)(_.id)
  def clusterFK = foreignKey("fk_post_cluster", cluster, Cluster.table)(_.id)

  override def * : ProvenShape[PostConnection] = (id, caption, song, poster, clique, cluster, createdAt).mapTo[PostConnection]
}

