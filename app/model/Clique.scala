package model

import slick.collection.heterogeneous.HNil
import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._

class Clique(id: Int)(implicit ec: ExecutionContext, db: Database) extends EntityInstance(id, Clique.tableQuery){
  lazy val otherVal: Property[Int] = property(Clique.otherVal)

}
object Clique extends Entity[Clique]("Clique"){
  val otherVal = Attribute[Int](root, "other")

  override type Connection = otherVal.next
  override type TableType = CliqueTable

  class CliqueTable(tag: Tag) extends EntityTable[Clique.Connection](tag, Clique) {
    private def otherVal = column(Clique.otherVal)

    override def * = otherVal :: id :: HNil
  }

  override def apply(id: Int)(implicit ec: ExecutionContext, db: Database): Clique = new Clique(id)
}

