package com.netease.wm.util

import java.sql.{Connection, ResultSet}
import java.util.Date

import shapeless.labelled.{FieldType, field}
import shapeless.ops.record.ToMap
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

import scala.annotation.implicitNotFound
import scala.util.{Success, Try}
import scala.language.implicitConversions

/**
  * Sql util for query jdbc result set into case class, example:
  *
  *
  *  Class.forName("org.h2.Driver")
  *  implicit val conn: Connection = DriverManager.getConnection("jdbc:h2:mem:test")
  *
  *  import Sql._
  *  Sql.execute("create table book(id varchar(255), title varchar(255))")
  *  Sql.execute("insert into book values('123', 'abc')")
  *
  *  queryAll[Book]("select * from book")
  *
  *  val b2 = Book("100", "Programming Scala")
  *  sql"insert into book values(${0}, ${1})".update(param(b2))
  *  sql"insert into book values(${'id}, ${1})".update(param(b2))
  *  sql"""insert into book values(${"id"}, ${1})""".update(param(b2))
  *
  * Created by hzxiaonaitong on 2017/3/28.
  */
object Sql {
  implicit def read(sql: String)(implicit conn: Connection): ResultSet = {
    conn.prepareStatement(sql).executeQuery()
  }

  def execute(sql: String)(implicit conn: Connection): Unit = {
    conn.prepareCall(sql).execute()
  }

  implicit class SqlHelper(val sc: StringContext) extends AnyVal {
    def sql(args: Any*): SqlOps = {
      val sql = sc.parts.mkString("?")
      SqlOps(sql, args)
    }
  }

  trait SqlParam {
    def get(i: Int): Any
    def get(name: String): Any
  }

  private case class ProductSqlParam[T <: Product, L <: HList](param: T)(
    implicit val gen: LabelledGeneric.Aux[T, L], ltmr: ToMap[L]) extends SqlParam {
    val tmr: ToMap[L] = ltmr
    val m: Map[tmr.Key, tmr.Value] = tmr(gen.to(param))
    val paramMap: Map[String, tmr.Value] = m.map { case (k: Symbol, v) => k.name -> v }

    override def get(i: Int): Any = {
      require(i < param.productArity, s"Not enough args for index $i with param size ${param.productArity}")
      param.productElement(i) match {
        case v: Option[_] if v.isEmpty => null
        case v: Option[_] if v.isDefined => v.get
        case v => v
      }
    }

    override def get(name: String): Any = {
      require(paramMap.contains(name), s"No arg $name found in param")
      paramMap(name) match {
        case v: Option[_] if v.isEmpty => null
        case v: Option[_] if v.isDefined => v.get
        case v => v
      }
    }
  }

  implicit class MapSqlParam(m: Map[String, Any]) extends SqlParam{
    override def get(i: Int): Any = throw new RuntimeException("unsupported sql param")
    override def get(name: String): Any = m.get(name)
  }

  implicit def param(p: String): SqlParam = param(Tuple1(p))
  implicit def param(p: Int): SqlParam = param(Tuple1(p))
  implicit def param(p: Long): SqlParam = param(Tuple1(p))
  implicit def param[T <: Product, L <: HList](param: T)(implicit gen: LabelledGeneric.Aux[T, L], tmr: ToMap[L]): SqlParam = ProductSqlParam(param)

  case class SqlOps private(sql: String, keys: Seq[Any]) {
    def query[T]()(implicit conn: Connection, extractor: Extractor[T]): Seq[T] = {
      val stmt = conn.prepareStatement(sql)
      val resultSet = stmt.executeQuery()
      try {
        queryAll[T](resultSet).toList
      } finally {
        stmt.close()
      }
    }

    def query[T](param: SqlParam)(implicit conn: Connection, extractor: Extractor[T]): Seq[T] = {
      val stmt = conn.prepareStatement(sql)
      try {
        val args = keys.map {
          case n: Int => param.get(n)
          case symbol: Symbol => param.get(symbol.name)
          case key: String => param.get(key)
          case a => throw new RuntimeException(s"sql unresolved args $a")
        }

        args.zipWithIndex foreach {
          case (None, index) => stmt.setObject(index + 1, null)
          case (v, index) => stmt.setObject(index + 1, v)
        }
        val resultSet = stmt.executeQuery()
        queryAll[T](resultSet).toList
      } finally {
        stmt.close()
      }
    }

    def update(param: SqlParam)(implicit conn: Connection): Unit = {
      val stmt = conn.prepareStatement(sql)
      try {
        val args = keys.map {
          case n: Int => param.get(n)
          case symbol: Symbol => param.get(symbol.name)
          case key: String => param.get(key)
          case a => throw new RuntimeException(s"sql unresolved args $a")
        }

        args.zipWithIndex foreach {
          case (None, index) => stmt.setObject(index + 1, null)
          case (v, index) => stmt.setObject(index + 1, v)
        }
        stmt.execute()
      } finally {
        stmt.close()
      }
    }

    def updateWithReply(param: SqlParam)(implicit conn: Connection): Int = {
      val stmt = conn.prepareStatement(sql)
      try {
        val args = keys.map {
          case n: Int => param.get(n)
          case symbol: Symbol => param.get(symbol.name)
          case key: String => param.get(key)
          case a => throw new RuntimeException(s"sql unresolved args $a")
        }

        args.zipWithIndex foreach {
          case (None, index) => stmt.setObject(index + 1, null)
          case (v, index) => stmt.setObject(index + 1, v)
        }
        stmt.executeUpdate()
      } finally {
        stmt.close()
      }
    }

    def batchUpdate(params: Seq[SqlParam])(implicit conn: Connection): Unit = {
      val stmt = conn.prepareStatement(sql)
      try {
        params.foreach{ param =>
          val args = keys.map {
            case n: Int => param.get(n)
            case symbol: Symbol => param.get(symbol.name)
            case key: String => param.get(key)
            case a => throw new RuntimeException(s"sql unresolved args $a")
          }

          args.zipWithIndex foreach {
            case (v, index) => stmt.setObject(index + 1, v)
          }
          stmt.addBatch()
        }
        stmt.executeBatch()
      } finally {
        stmt.close()
      }
    }

    def execute()(implicit conn: Connection): Unit = {
      val stmt = conn.prepareStatement(sql)
      try {
        stmt.execute()
      } finally {
        stmt.close()
      }
    }

    def stripMargin(marginChar: Char): SqlOps = copy(sql = sql.stripMargin(marginChar))
    def stripMargin: SqlOps = copy(sql = sql.stripMargin)
  }

  def query[A](result: ResultSet, column: String = "")(implicit extractor: Extractor[A]): A = extractor.query(result, column)

  def queryAll[A](result: ResultSet, column: String = "")(implicit extractor: Extractor[A]): Seq[A] =
    Iterator.continually{ result.next() }
      .takeWhile(identity)
      .map{ _ => extractor.query(result, column) }
      .toSeq

  // TODO why Extractor can't be covariant
  @implicitNotFound(msg = "Cannot construct Extractor for type ${A}, currently only " +
    "primitive types or option of primitives or product type are supported")
  trait Extractor[A] {
    def query(result: ResultSet, column: String = ""): A
  }

  // tagging trait for atom type
  trait AtomExtractor[A] extends Extractor[A]

  def createExtractor[A](f: (ResultSet,String) => A): Extractor[A] = new Extractor[A] {
    override def query(result: ResultSet, column: String): A = f(result, column)
  }

  def createAtomExtractor[A](f: (ResultSet,String) => A): AtomExtractor[A] = new AtomExtractor[A] {
    override def query(result: ResultSet, column: String): A =
      if(column.isEmpty) {
        throw new RuntimeException("column name is needed for query atom column")
      }
      else f(result, column)
  }

  // primitive extractors
  implicit val stringExtractor: AtomExtractor[String] = createAtomExtractor[String]{ (result, column) => result.getString(column) }
  implicit val longExtractor: AtomExtractor[Long] = createAtomExtractor[Long]{ (result, column) => result.getLong(column) }
  implicit val intExtractor: AtomExtractor[Int] = createAtomExtractor[Int]{ (result, column) => result.getInt(column) }
  implicit val shortExtractor: AtomExtractor[Short] = createAtomExtractor[Short]{ (result, column) => result.getShort(column) }
  implicit val byteExtractor: AtomExtractor[Byte] = createAtomExtractor[Byte]{ (result, column) => result.getByte(column) }
  implicit val floatExtractor: AtomExtractor[Float] = createAtomExtractor[Float]{ (result, column) => result.getFloat(column) }
  implicit val doubleExtractor: AtomExtractor[Double] = createAtomExtractor[Double]{ (result, column) => result.getDouble(column) }
  implicit val boolExtractor: AtomExtractor[Boolean] = createAtomExtractor[Boolean]{ (result, column) => result.getBoolean(column) }
  implicit val dateExtractor: AtomExtractor[Date] = createAtomExtractor[Date]{ (result, column) => result.getDate(column) }

  // option extractors
  // TODO only option of primitive is supported
  implicit def optionExtractor[A](implicit extractor: AtomExtractor[A]): Extractor[Option[A]] =
  createExtractor {
    (result, column) =>
      val raw = Try{ extractor.query(result, column) }
      raw match {
        case Success(value) if !result.wasNull() => Some(value)
        case _ => None
      }
  }

  // product extractors
  implicit val hnilExtractor: Extractor[HNil] = createExtractor[HNil]{ (_, _) => HNil }
  implicit def hlistExtractor[K <: Symbol, H, T <: HList](implicit witness: Witness.Aux[K],
                                                          hExtractor: Lazy[Extractor[H]],
                                                          tExtractor: Extractor[T]): Extractor[FieldType[K, H] :: T] = {
    val fieldName = witness.value.name
    createExtractor {
      (result, prefix) =>
        val column = if (prefix.isEmpty) fieldName else s"${prefix}_$fieldName"
        val part: FieldType[K,H] = field[K](hExtractor.value.query(result, column))
        part :: tExtractor.query(result, prefix)
    }
  }

  implicit def genericProductExtractor[A, H <: HList](implicit generic: LabelledGeneric.Aux[A, H],
                                                      hExtractor: Lazy[Extractor[H]]): Extractor[A] =
    createExtractor { (result, column) =>
      val fields = hExtractor.value.query(result, column)
      generic.from(fields)
    }

}
