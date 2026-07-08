package com.netease.music.da.transfer.ddb.dbi.writer

import java.sql.{Connection, SQLException}

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.ddb.dbi.conf.DDBDBIProperties._
import com.netease.music.da.transfer.ddb.dbi.connection.DDBDBIConnection
import com.netease.music.da.transfer.ddb.dbi.utils.JdbcUtils
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties._
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.writer.JDBCWriter
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.sql.jdbc.{JdbcDialect, JdbcDialects}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.util.control.NonFatal

class DDBDBIWriter(@transient spark: SparkSession) extends JDBCWriter(spark) {
  override def confPrefix: String = "spark.transmit.writer.ddb.dbi"

  override def addDefaultProperties(properties: Properties) {
    super.addDefaultProperties(properties)
    properties.put(NEED_REPARTITION.key, "true")
    properties.put(REPARTITION_NUM.key, "1")
  }

  override def createDBConnection(properties: Properties): DBConnection = {
    new DDBDBIConnection(properties)
  }

  def getInsertStatement(table: String,
                         rddSchema: StructType,
                         batchSize: Int,
                         isCaseSensitive: Boolean,
                         dialect: JdbcDialect): String = {

    def getStatement(prefix: String): String = {
      val columns =
        rddSchema.fields.map(x => dialect.quoteIdentifier(x.name)).mkString(",")
      val placeholder = rddSchema.fields.map(_ => "?").mkString("(", ",", ")")
      val placeholders = Array.fill(batchSize)(placeholder).mkString(", ")
      s"$prefix $table ($columns) VALUES $placeholders"
    }

    this.properties.getProperty(SAVE_MODE).get match {
      case "insertInto" =>
        getStatement("INSERT INTO")
      case "replaceInto" =>
        getStatement("REPLACE INTO")
      case "insertIgnore" =>
        getStatement("INSERT IGNORE INTO")
      case value =>
        throw new IllegalArgumentException(s"Unsupported value `$value` for parameter `${SAVE_MODE.key}`.")
    }
  }

  def savePartition(getConnection: () => Connection,
                    table: String,
                    iterator: Iterator[Row],
                    rddSchema: StructType,
                    batchSize: Int,
                    isCaseSensitive: Boolean,
                    isolationLevel: Int): Iterator[Byte] = {
    // fake mysql dialect
    val dialect = JdbcDialects.get("jdbc:mysql://localhost")
    val conn = getConnection()
    var committed = false

    var finalIsolationLevel = Connection.TRANSACTION_NONE
    if (isolationLevel != Connection.TRANSACTION_NONE) {
      try {
        val metadata = conn.getMetaData
        if (metadata.supportsTransactions()) {
          // Update to at least use the default isolation, if any transaction level
          // has been chosen and transactions are supported
          val defaultIsolation = metadata.getDefaultTransactionIsolation
          finalIsolationLevel = defaultIsolation
          if (metadata.supportsTransactionIsolationLevel(isolationLevel)) {
            // Finally update to actually requested level if possible
            finalIsolationLevel = isolationLevel
          } else {
            LOG.warn(s"Requested isolation level $isolationLevel is not supported; " +
              s"falling back to default isolation level $defaultIsolation")
          }
        } else {
          LOG.warn(s"Requested isolation level $isolationLevel, but transactions are unsupported")
        }
      } catch {
        case NonFatal(e) => LOG.warn("Exception while detecting transaction support", e)
      }
    }
    val supportsTransactions = finalIsolationLevel != Connection.TRANSACTION_NONE

    try {
      if (supportsTransactions) {
        conn.setAutoCommit(false) // Everything in the same db transaction.
        conn.setTransactionIsolation(finalIsolationLevel)
      }

      val setters = rddSchema.fields.map(f => JdbcUtils.makeSetter(conn, dialect, f.dataType))
      val nullTypes = rddSchema.fields.map(f => JdbcUtils.getJdbcType(f.dataType, dialect).jdbcNullType)
      val numFields = rddSchema.fields.length
      iterator.grouped(batchSize).foreach { group =>
        val size = group.size
        val stmt =
          conn.prepareStatement(getInsertStatement(table, rddSchema, size, isCaseSensitive, dialect))
        try {
          var stmtPos = 1
          group.foreach { row =>
            var rowPos = 0
            while (rowPos < numFields) {
              if (row.isNullAt(rowPos)) {
                stmt.setNull(stmtPos, nullTypes(rowPos))
              } else {
                setters(rowPos).apply(stmt, row, rowPos, stmtPos)
              }
              rowPos += 1
              stmtPos += 1
            }
          }
          stmt.execute()
        } finally {
          stmt.close()
        }
      }

      if (supportsTransactions) {
        conn.commit()
      }
      committed = true
      Iterator.empty
    } catch {
      case e: SQLException =>
        val cause = e.getNextException
        if (cause != null && e.getCause != cause) {
          // If there is no cause already, set 'next exception' as cause. If cause is null,
          // it *may* be because no cause was set yet
          if (e.getCause == null) {
            try {
              e.initCause(cause)
            } catch {
              // Or it may be null because the cause *was* explicitly initialized, to *null*,
              // in which case this fails. There is no other way to detect it.
              // addSuppressed in this case as well.
              case _: IllegalStateException => e.addSuppressed(cause)
            }
          } else {
            e.addSuppressed(cause)
          }
        }
        throw e
    } finally {
      if (!committed) {
        // The stage must fail.  We got here through an exception path, so
        // let the exception through unless rollback() or close() want to
        // tell the user about another problem.
        if (supportsTransactions) {
          conn.rollback()
        }
        conn.close()
      } else {
        // The stage must succeed.  We cannot propagate any exception close() might throw.
        try {
          conn.close()
        } catch {
          case e: Exception => LOG.warn("Transaction succeeded, but closing failed", e)
        }
      }
    }
  }

  override def write(data: DataFrame): Unit = {
    val tableName = this.properties.getProperty(TABLE).get
    val database = this.properties.getProperty(DATABASE)
    val tableIdentifier = TableIdentifier(tableName, database)
    val table = tableIdentifier.quotedString
    val rddSchema = data.schema
    val getConnection: () => Connection = () => {
      getOrCreateDBConnection(this.properties).connection
    }
    val batchSize = this.properties.getProperty(BATCH_SIZE).get
    val isolationLevel = this.properties.getProperty(ISOLATION_LEVEL).get
    val isCaseSensitive = spark.sessionState.conf.getConf(SQLConf.CASE_SENSITIVE)
    // fake dialect as mysql dialect

    val repartitionedData = if (this.properties.getProperty(NEED_REPARTITION).get) {
      this.properties.getProperty(REPARTITION_NUM) match {
        case Some(n) if n <= 0 =>
          LOG.warn(s"Invalid value `$n` for parameter `${REPARTITION_NUM.key}` in table writing " +
            "via JDBC. The minimum value is 1, skip it.")
          data
        case Some(n) if n < data.rdd.getNumPartitions =>
          LOG.info(s"Repartitioned data set with size $n")
          data.coalesce(n)
        case Some(n) if n >= data.rdd.getNumPartitions =>
          LOG.warn(s"The value `$n` for parameter `${REPARTITION_NUM.key}` in table writing " +
            "via JDBC is bigger than or equals to origin partition size, skip it.")
          data
      }
    } else {
      data
    }
    doPreSQL()
    repartitionedData.rdd.foreachPartition(iterator =>
      savePartition(getConnection, table, iterator, rddSchema, batchSize, isCaseSensitive, isolationLevel))
    doPostSQL()
  }
}
