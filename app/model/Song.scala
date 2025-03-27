package model

import slick.collection.heterogeneous.HNil
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._

class Song(id: Int)(implicit ec: ExecutionContext, db: Database) extends EntityInstance(id, Song.tableQuery){
  lazy val otherVal: Property[Int] = property(Song.otherVal)

}
object Song extends Entity[Song]("Song") {
  val otherVal = Attribute[Int](root, "other")

  override type Connection = otherVal.next
  override type TableType = SongTable

  class SongTable(tag: Tag) extends EntityTable[Song.Connection](tag, Song) {
    private def otherVal = column(Song.otherVal)

    override def * = otherVal :: id :: HNil
  }

  override def apply(id: Int)(implicit ec: ExecutionContext, db: MySQLProfile.api.Database): Song = new Song(id)
}
