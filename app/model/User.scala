package model

import slick.collection.heterogeneous.HNil
import slick.jdbc.MySQLProfile

import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._

class User(id: Int)(implicit ec: ExecutionContext, db: Database) extends EntityInstance(id, User.tableQuery){
  lazy val username: Property[Int] = property(User.username)
  lazy val hashedKey: Property[String] = property(User.hashedKey)

}
object User extends Entity[User]("User") {
  val username = Attribute[Int](root, "username")
  val hashedKey = Attribute[String](username, "hashedKey")

  override type Connection = hashedKey.next
  override type TableType = UserTable

  class UserTable(tag: Tag) extends EntityTable[User.Connection](tag, User) {
    private def username = column(User.username)
    private def hashedKey = column(User.hashedKey)

    override def * = hashedKey :: username :: id :: HNil
  }

  override def apply(id: Int)(implicit ec: ExecutionContext, db: MySQLProfile.api.Database): User = new User(id)
}
