package model

import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.MySQLProfile.api._


case class Identity[T](value: String) {
  override def equals(obj: Any): Boolean = obj match {
    case that: Identity[_] => this.value == that.value
    case _ => false
  }
}

object Identity {
  implicit def identityMapping[T]: JdbcType[Identity[T]] with BaseTypedType[Identity[T]] = MappedColumnType.base[Identity[T], String](
    identity => identity.value,
    str => Identity[T](str)
  )

  implicit def eitherIdentityMapping[L, R]: JdbcType[Either[Identity[L], Identity[R]]] with BaseTypedType[Either[Identity[L], Identity[R]]] = 
    MappedColumnType.base[Either[Identity[L], Identity[R]], String](
      {
        case Left(identity) => s"A:${identity.value}"
        case Right(identity) => s"B:${identity.value}"
      },
      str => str.split(":", 2) match {
        case Array("A", value) => Left(Identity[L](value))
        case Array("B", value) => Right(Identity[R](value))
        case _ => throw new IllegalArgumentException(s"Invalid Either Identity format: $str")
      }
    )
}


