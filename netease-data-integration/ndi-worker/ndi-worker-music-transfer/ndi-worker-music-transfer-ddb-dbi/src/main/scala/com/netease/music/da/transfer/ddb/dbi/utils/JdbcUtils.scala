package com.netease.music.da.transfer.ddb.dbi.utils

import java.sql.{Connection, PreparedStatement}
import java.util.Locale

import org.apache.spark.sql.Row
import org.apache.spark.sql.jdbc.{JdbcDialect, JdbcType}
import org.apache.spark.sql.types._

/**
  * Copied from [[org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils]]
  *
  */
object JdbcUtils {
  def getJdbcType(dt: DataType, dialect: JdbcDialect): JdbcType = {
    dialect.getJDBCType(dt)
      .orElse(org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils.getCommonJDBCType(dt))
      .getOrElse(
        throw new IllegalArgumentException(s"Can't get JDBC type for ${
          dt.simpleString
        }"))
  }

  type JDBCValueSetter = (PreparedStatement, Row, Int, Int) => Unit

  def makeSetter(conn: Connection,
                         dialect: JdbcDialect,
                         dataType: DataType): JDBCValueSetter = dataType match {
    case IntegerType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setInt(stmtPos, row.getInt(rowPos))

    case LongType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setLong(stmtPos, row.getLong(rowPos))

    case DoubleType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setDouble(stmtPos, row.getDouble(rowPos))

    case FloatType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setFloat(stmtPos, row.getFloat(rowPos))

    case ShortType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setInt(stmtPos, row.getShort(rowPos))

    case ByteType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setInt(stmtPos, row.getByte(rowPos))

    case BooleanType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setBoolean(stmtPos, row.getBoolean(rowPos))

    case StringType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setString(stmtPos, row.getString(rowPos))

    case BinaryType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setBytes(stmtPos, row.getAs[Array[Byte]](rowPos))

    case TimestampType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setTimestamp(stmtPos, row.getAs[java.sql.Timestamp](rowPos))

    case DateType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setDate(stmtPos, row.getAs[java.sql.Date](rowPos))

    case t: DecimalType =>
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        stmt.setBigDecimal(stmtPos, row.getDecimal(rowPos))

    case ArrayType(et, _) =>
      // remove type length parameters from end of type name
      val typeName = getJdbcType(et, dialect).databaseTypeDefinition
        .toLowerCase(Locale.ROOT).split("\\(")(0)
      (stmt: PreparedStatement, row: Row, rowPos: Int, stmtPos: Int) =>
        val array = conn.createArrayOf(
          typeName,
          row.getSeq[AnyRef](rowPos).toArray)
        stmt.setArray(stmtPos, array)

    case _ =>
      (_: PreparedStatement, _: Row, rowPos: Int, stmtPos: Int) =>
        throw new IllegalArgumentException(
          s"Can't translate non-null value for field $rowPos")
  }
}
