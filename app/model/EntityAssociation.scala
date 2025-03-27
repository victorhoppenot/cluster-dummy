package model

import slick.ast.{ColumnOption, TypedType}
import slick.collection.heterogeneous.syntax.HNil
import slick.collection.heterogeneous.{HCons, HList, HNil}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}


class Attribute[T, P <: HList](prev: Option[Attribute[?, ?]], val name: String) {
  type next = HCons[T, P]

  val idx: Int = prev match {
    case None => 0
    case Some(attr) => attr.idx + 1
  }
}

class ForeignAttribute[P <: HList, E <: Entity[?]](prev: Option[Attribute[?, ?]], name: String, val entity: E) extends Attribute[Int, P](prev, name)

object Attribute {
  def apply[T](name: String): Attribute[T, HNil] = new Attribute(None, name)
  def apply[T](prev: Attribute[?,?], name: String) = new Attribute[T, prev.next](Some(prev), name)
  def apply[E <: Entity[?]](prev: Attribute[?,?], name: String, entity : E) = new ForeignAttribute[prev.next, E](Some(prev), name, entity)
}

class Property[T](val get: () => Future[Option[T]], val set: T => Future[Boolean])(implicit ec: ExecutionContext, db: Database)

abstract class Entity[EI <: EntityInstance[?,?]](val name: String) {

  type Connection <: HList
  type TableType <: EntityTable[Connection]

  def apply(id: Int)(implicit ec: ExecutionContext, db: Database): EI

  val id: Attribute[Int, HNil] = Attribute[Int]("id")
  def root: Attribute[Int, HNil] = id



  lazy val tableQuery = TableQuery[TableType]
}

abstract class EntityTable[C](tag: Tag, entity: Entity[?]) extends Table[C](tag, entity.name){
  def id = column(entity.id, O.PrimaryKey, O.AutoInc)


  def column[T](attribute: Attribute[T,?], options: ColumnOption[T]*)(implicit t: TypedType[T]): Rep[T] = column[T](attribute.name)
  def foreignKey[E <: Entity[?]](attribute: ForeignAttribute[?, E], col: => Rep[Int]) =
    foreignKey(attribute.name, col, attribute.entity.tableQuery)(_.id,ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
}

abstract class EntityInstance[C <: HList, E <: EntityTable[C]](val id: Int, tableQuery: TableQuery[E])(implicit ec: ExecutionContext, db: Database) {

  private def fetchConnection(): Future[Option[C]]  = {
    val query = tableQuery.filter(_.id === id).result.headOption
    db.run(query)
  }

  private def updateConnection(conn: C): Future[Int] = {
    val query = tableQuery += conn
    db.run(query)
  }

  def doesExist: Future[Boolean] = {
    fetchConnection().map({
      case None => false
      case Some => true
    })
  }

  def getter[T](attribute: Attribute[T,?]): () => Future[Option[T]] = () => {
    fetchConnection().map(_.map(_.productElement(attribute.idx).asInstanceOf[T]))
  }

  def setter[T](attribute: Attribute[T,?]): T => Future[Boolean] = t => {
    fetchConnection().map(_.map(conn => {
      val idx = 0
      val newConn: C = HList.hnilShape.buildValue(conn.productIterator.map(value => {
        if(idx == attribute.idx){
          t
        }else{
          value
        }
      }).toIndexedSeq).asInstanceOf[C]
      updateConnection(newConn)
    }).getOrElse(Future(0))).flatten.map(_ > 0)
  }

  def property[T](attribute: Attribute[T,?]): Property[T] = {
    new Property[T](getter(attribute), setter(attribute))
  }

  def property[X <: Entity[EI], EI <: EntityInstance[?,?]](attribute: ForeignAttribute[?, X]): Property[EI] = {
    val getId = getter(attribute)
    val setId = setter(attribute)
    new Property[EI](() => getId().map(_.map(attribute.entity.apply)), ins => setId(ins.id))
  }
}

abstract class Association[AI <: AssociationInstance[?,?]](val name: String, val subjEntity: Entity[?], val objEntity: Entity[?]) {
  val subj: Attribute[Int, HNil] = Attribute[Int]("subj")
  val obj: Attribute[Int, subj.next] = Attribute[Int](subj, "obj")
  def root: Attribute[Int, subj.next] = obj

  type Connection
  type TableType <: AssociationTable[Connection]
  lazy val tableQuery = TableQuery[TableType]

  def apply(subj: Int, obj: Int)(implicit ec: ExecutionContext, db: Database): AI
  def apply(subj: Int)
}

abstract class AssociationTable[C](tag: Tag, assoc: Association[?]) extends Table[C](tag, assoc.name) {
  def subj = column(assoc.subj)
  def obj = column(assoc.obj)

  def pk = primaryKey("pk", (subj,obj))

  def subjFk = foreignKey("subj_fk", subj, assoc.subjEntity.tableQuery)(_.id,ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
  def objFk = foreignKey("obj_fk", obj, assoc.objEntity.tableQuery)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)

  def column[T](attribute: Attribute[T,?], options: ColumnOption[T]*)(implicit t: TypedType[T]): Rep[T] = column[T](attribute.name)
}

abstract class AssociationInstance[C <: HList, E <: AssociationTable[C]](val subj: Int, val obj: Int, tableQuery: TableQuery[E])(implicit ec: ExecutionContext, db: Database) {

  private def fetchConnection(): Future[Option[C]]  = {
    val query = tableQuery.filter(r => r.subj === subj && r.obj === obj).result.headOption
    db.run(query)
  }

  private def updateConnection(conn: C): Future[Int] = {
    val query = tableQuery += conn
    db.run(query)
  }

  def doesExist: Future[Boolean] = {
    fetchConnection().map({
      case None => false
      case Some => true
    })
  }

  def getter[T](attribute: Attribute[T,?]): () => Future[Option[T]] = () => {
    fetchConnection().map(_.map(_.productElement(attribute.idx).asInstanceOf[T]))
  }

  def setter[T](attribute: Attribute[T,?]): T => Future[Boolean] = t => {
    fetchConnection().map(_.map(conn => {
      val idx = 0
      val newConn: C = HList.hnilShape.buildValue(conn.productIterator.map(value => {
        if(idx == attribute.idx){
          t
        }else{
          value
        }
      }).toIndexedSeq).asInstanceOf[C]
      updateConnection(newConn)
    }).getOrElse(Future(0))).flatten.map(_ > 0)
  }

  def property[T](attribute: Attribute[T,?]): Property[T] = new Property[T](getter(attribute), setter(attribute))

  def foreignProperty[X <: Entity[EI], EI <: EntityInstance[?,?]](attribute: ForeignAttribute[?, X]): Unit = {
    val getId = getter(attribute)
    val setId = setter(attribute)
    new Property[EI](() => getId().map(_.map(attribute.entity.apply)), ins => setId(ins.id))
  }
}

class AssociationSubjectSet[C <: HList, E <: AssociationTable[C]](subj: Int, tableQuery: TableQuery[E])(implicit ec: ExecutionContext, db: Database) {
  def objects(): Future[Seq[Int]] = {
    val query = tableQuery.filter(r => r.subj === subj).result
    db.run(query).map(seq => {
      seq map {
        _.productElement(0).asInstanceOf[Int]
      }
    })
  }
}

class AssociationObjectSet[C <: HList, E <: AssociationTable[C]](obj: Int, tableQuery: TableQuery[E])(implicit ec: ExecutionContext, db: Database) {
  def subjects(): Future[Seq[Int]] = {
    val query = tableQuery.filter(r => r.obj === obj).result
    db.run(query).map(seq => {
      seq map {
        _.productElement(0).asInstanceOf[Int]
      }
    })
  }
}