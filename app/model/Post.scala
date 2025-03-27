package model

import slick.collection.heterogeneous.HNil
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._


class Post(id: Int)(implicit ec: ExecutionContext, db: Database) extends EntityInstance(id, Post.tableQuery){
  lazy val poster: Property[User] = property(Post.poster)
  lazy val song: Property[Song] = property(Post.song)
  lazy val cluster: Property[Cluster] = property(Post.cluster)
  lazy val clique: Property[Clique] = property(Post.clique)
  lazy val caption: Property[Comment] = property(Post.caption)

}
object Post extends Entity[Post]("Post") {
  val poster = Attribute(root, "poster", User)
  val song = Attribute(poster, "song", Song)
  val cluster = Attribute(song, "cluster", Cluster)
  val clique = Attribute(cluster, "clique", Clique)
  val caption = Attribute(clique, "caption", Comment)

  override type Connection = caption.next
  override type TableType = PostTable

  class PostTable(tag: Tag) extends EntityTable[Post.Connection](tag, Post) {
    private def otherVal = column(Post.otherVal)

    override def * = otherVal :: id :: HNil
  }

  override def apply(id: Int)(implicit ec: ExecutionContext, db: MySQLProfile.api.Database): Post = new Post(id)
}

