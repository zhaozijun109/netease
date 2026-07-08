package com.netease.music.da.transfer.common.metrics

import java.sql.{Connection, DriverManager}

import com.netease.music.da.transfer.common.log.LogTrait
import org.apache.spark.SparkConf

import scala.util.Try

object MySQLMetricsRecorder extends MetricsRecorder with LogTrait {
  override def record(sparkConf: SparkConf): Unit = {
    var connection: Connection = null
    try {
      val url = sparkConf.get("spark.transmit.metrics.url")
      val user = sparkConf.get("spark.transmit.metrics.username")
      val password = sparkConf.get("spark.transmit.metrics.password")
      Class.forName("com.mysql.jdbc.Driver")
      connection = DriverManager.getConnection(url, user, password)
      val statement = connection.prepareStatement(
        """
          |INSERT INTO
          |  execution_metrics(`task_id`, `start_time`, `end_time`, `input_byte_number`,
          |    input_record_number, `output_byte_number`, `output_record_number`, `tags`)
          |VALUES
          |  (?, ?, ?, ?, ?, ?, ?, ?)
        """.stripMargin)
      statement.setString(1, sparkConf.get("spark.transmit.metrics.taskId"))
      statement.setLong(2, Metrics.startTime)
      statement.setLong(3, Metrics.endTime)
      statement.setLong(4, Metrics.inputMetrics.byteNumber)
      statement.setLong(5, Metrics.inputMetrics.recordNumber)
      statement.setLong(6, Metrics.outputMetrics.byteNumber)
      statement.setLong(7, Metrics.outputMetrics.recordNumber)
      statement.setString(8, sparkConf.get("spark.transmit.metrics.tags", null))
      statement.execute()
      statement.close()
      connection.close()
    } catch {
      case e: Exception =>
        LOG.warn("Record transmit metrics failed.", e)
    } finally {
      Try {
        if (connection != null) {
          connection.close()
        }
      }
    }
  }
}
