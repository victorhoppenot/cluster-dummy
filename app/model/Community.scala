package model

import slick.collection.heterogeneous.HNil
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._


class Community(id: Int)(implicit ec: ExecutionContext, db: Database) extends EntityInstance(id, Community.tableQuery){
  lazy val otherVal: Property[Int] = property(Community.otherVal)

}

object Community extends Entity[Community]("Community") {
  val otherVal = Attribute[Int](root, "other")

  override type Connection = otherVal.next
  override type TableType = CommunityTable

  class CommunityTable(tag: Tag) extends EntityTable[Community.Connection](tag, Community) {
    private def otherVal = column(Community.otherVal)

    override def * = otherVal :: id :: HNil
  }

  override def apply(id: Int)(implicit ec: ExecutionContext, db: MySQLProfile.api.Database): Community = new Community(id)
}
