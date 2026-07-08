package com.netease.music.da.transfer.ddb.qs.reader

import com.netease.music.da.transfer.common.conf.Properties
import com.netease.music.da.transfer.jdbc.conf.JDBCProperties
import com.netease.music.da.transfer.jdbc.connection.DBConnection
import com.netease.music.da.transfer.jdbc.reader.JDBCReader
import com.netease.music.da.transfer.mysql.conf.MySQLConstants
import com.netease.music.da.transfer.mysql.connection.MySQLConnection
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

class DDBQSReader(@transient spark: SparkSession) extends JDBCReader(spark) {
  override def confPrefix: String = "spark.transmit.reader.ddb.qs"

  override def addDefaultProperties(props: Properties): Unit = {
    super.addDefaultProperties(props)
    LOG.info(s"set fetch size to ${Integer.MIN_VALUE}")
    props.put(JDBCProperties.DRIVER.key, MySQLConstants.DEFAULT_DRIVER)
  }

  override def createDBConnection(properties: Properties): DBConnection = {
    new MySQLConnection(properties)
  }

  override def tableProperties: List[Properties] = {
    val ddbQSConnection = createDBConnection(this.properties)
    val table = this.properties.getProperty(JDBCProperties.TABLE).get
    val policy = ddbQSConnection.executeQuery("SHOW TABLES", { resultSet =>
      var policy: String = null
      while (resultSet.next() && policy == null) {
        if (resultSet.getString("NAME") == table) {
          policy = resultSet.getString("POLICY")
        }
      }
      if (policy == null) {
        LOG.error(s"Cannot find policy for table $table")
      } else {
        LOG.info(s"Policy: $policy")
      }
      policy
    })

    val dbns: List[String] = ddbQSConnection.executeQuery(s"DESC POLICY ${policy.get}", { resultSet =>
      val all = mutable.ArrayBuffer[String]()
      while (resultSet.next()) {
        all += resultSet.getString("URL")
      }
      all.distinct.toList
    }).get
    ddbQSConnection.closeQuietly()
    LOG.info(s"DBN: ${dbns.mkString(", ")}")
    dbns.map { dbn =>
      Properties()
        .putProperties(this.properties)
        .put(JDBCProperties.URL.key, dbn)
    }
  }
}