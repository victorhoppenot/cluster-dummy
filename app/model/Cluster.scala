package model

import slick.collection.heterogeneous.HNil
import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._


class Cluster(id: Int)(implicit ec: ExecutionContext, db: Database) extends EntityInstance(id, Cluster.tableQuery){
  lazy val otherVal: Property[Int] = property(Cluster.otherVal)

}
object Cluster extends Entity[Cluster]("Cluster") {
  val otherVal = Attribute[Int](root, "other")

  override type Connection = otherVal.next
  override type TableType = ClusterTable

  class ClusterTable(tag: Tag) extends EntityTable[Cluster.Connection](tag, Cluster) {
    private def otherVal = column(Cluster.otherVal)

    override def * = otherVal :: id :: HNil
  }

  override def apply(id: Int)(implicit ec: ExecutionContext, db: Database): Cluster = new Cluster(id)
}
