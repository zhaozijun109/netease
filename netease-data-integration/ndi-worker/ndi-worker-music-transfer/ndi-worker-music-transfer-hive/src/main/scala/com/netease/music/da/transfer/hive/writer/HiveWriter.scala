package com.netease.music.da.transfer.hive.writer

import com.netease.music.da.transfer.common.util.DataFrameUtil
import com.netease.music.da.transfer.common.writer.AbstractDataWriter
import com.netease.music.da.transfer.hive.conf.HiveProperties._
import com.netease.music.da.transfer.hive.conf.{InsertInto, InsertOverwrite}
import com.netease.music.da.transfer.hive.util.HiveUtil
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.{DataFrame, SparkSession}

class HiveWriter(spark: SparkSession) extends AbstractDataWriter(spark) {
  override def confPrefix: String = "spark.transmit.writer.hive"

  def doPreSQL(): Unit = {
    this.properties.getSerial(PRE_SQL).foreach { sql =>
      if (!StringUtils.isEmpty(sql)) {
        LOG.info(s"Execute preSql: $sql")
        spark.sql(sql).show()
      }
    }
  }

  def doPostSQL(): Unit = {
    this.properties.getSerial(POST_SQL).foreach { sql =>
      if (!StringUtils.isEmpty(sql)) {
        LOG.info(s"Execute postSql: $sql")
        spark.sql(sql).show()
      }
    }
  }


  override def write(data: DataFrame): Unit = {
    val database = this.properties.getProperty(DATABASE)
    val table = this.properties.getProperty(TABLE).get
    val tableIdentifier = TableIdentifier(table, database)
    val partition = this.properties.getProperty(PARTITION)
    val fileMerge = this.properties.getProperty(FILE_MERGE).get
    val saveSql = this.properties.getProperty(SAVE_MODE).get match {
      case InsertInto() =>
        "INSERT INTO"
      case InsertOverwrite() =>
        "INSERT OVERWRITE"
      case _ =>
        throw new IllegalArgumentException(s"Unexpected value of ${SAVE_MODE.key}")
    }

    var result: DataFrame = data

    if (fileMerge) {
      val number = this.properties.getProperty(FILE_MERGE_NUMBER).get
      LOG.info(s"Enable file merge. File merge number: $number")
      result = result.coalesce(number)
    }

    val tmpView = DataFrameUtil.createTempView(result)
    val sql = if (partition.isDefined) {
      s"""
         |$saveSql TABLE ${tableIdentifier.quotedString} PARTITION(${partition.get})
         |SELECT * FROM $tmpView
        """.stripMargin
    } else {
      s"""
         |$saveSql TABLE ${tableIdentifier.quotedString}
         |SELECT * FROM $tmpView
        """.stripMargin
    }
    val keepOwner = this.properties.getProperty(KEEP_OWNER).get
    val owner = if (keepOwner) {
      val msc = HiveUtil.createMSC()
      val owner = HiveUtil.getOwner(msc, tableIdentifier)
      LOG.info(s"The owner of $database.$table is $owner.")
      Option(msc -> owner)
    } else {
      Option.empty
    }
    doPreSQL()
    LOG.info(s"Execute sql: $sql")
    spark.sql(sql)
    owner.foreach { pair =>
      val msc = pair._1
      val owner = pair._2
      val result = HiveUtil.alertOwner(msc, tableIdentifier, owner)
      if (result) {
        LOG.info(s"Modify the owner of $database.$table to $owner")
      }
      HiveUtil.closeMSCQuietly(msc)
    }
    doPostSQL()
  }
}
