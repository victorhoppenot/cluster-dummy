package model

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class UserUserAssociation(val id: Identity[UserUserAssociation])(implicit ec: ExecutionContext, database: Database) {

  def connection: Future[Option[UserUserAssociationConnection]] = {
    val query = UserUserAssociation.table.filter(_.id === id).result.headOption
    database.run(query)
  }

}

object UserUserAssociation {
  lazy val table = TableQuery[UserUserAssociationTable]
}

case class UserUserAssociationConnection(
                                       subj: Identity[User],
                                       obj: Identity[User],
                                       follows: Boolean,
                                       createdAt: java.time.LocalDateTime
                                     )

class UserUserAssociationTable(tag: Tag) extends Table[UserUserAssociationConnection](tag, "user_user_association") {
  def subj = column[Identity[User]]("subj")
  def obj = column[Identity[User]]("obj")
  def follows = column[Boolean]("follows")
  def createdAt = column[java.time.LocalDateTime]("created_at")

  def pk = primaryKey("pk_user_user", (subj, obj))
  def subjFK = foreignKey("fk_user_user_subj", subj, User.table)(_.id)
  def objFK = foreignKey("fk_user_user_obj", obj, User.table)(_.id)

  override def * : ProvenShape[UserUserAssociationConnection] = (subj, obj, follows, createdAt).mapTo[UserUserAssociationConnection]
} 