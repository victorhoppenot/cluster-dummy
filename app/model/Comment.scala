package model

import slick.collection.heterogeneous.HNil
import scala.concurrent.ExecutionContext
import slick.jdbc.MySQLProfile.api._


class Comment(id: Int)(implicit ec: ExecutionContext, db: Database) extends EntityInstance(id, Comment.tableQuery){
  lazy val otherVal: Property[Int] = property(Comment.otherVal)

}
object Comment extends Entity[Comment]("Comment") {
  val otherVal = Attribute[Int](root, "other")

  override type Connection = otherVal.next
  override type TableType = CommentTable

  class CommentTable(tag: Tag) extends EntityTable[Comment.Connection](tag, Comment) {
    private def otherVal = column(Comment.otherVal)

    override def * = otherVal :: id :: HNil
  }

  override def apply(id: Int)(implicit ec: ExecutionContext, db: Database): Comment = new Comment(id)
}
