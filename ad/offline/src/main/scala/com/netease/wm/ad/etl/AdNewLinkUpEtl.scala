package com.netease.wm.ad.etl

import com.netease.wm.util.Args
import org.apache.spark.sql.{SaveMode, SparkSession}

import scala.util.matching.Regex

object AdNewLinkUpEtl {
  def main(args: Array[String]): Unit = {
      val LOG_PATTERN: Regex = """^\d+-\d+-\d+\s+\d+:\d+:\d+,\d+\s+INFO\s+\(.*\)\[statLog\]\s+-\s+(.*)$""".r
      val pargs = Args(args)
      val spark = SparkSession.builder()
        .appName("Adx New Linkup Etl")
        .getOrCreate()

      val input = pargs.required("input")
      val output = pargs.required("output")

      import spark.implicits._

      spark.read.textFile(input)
        .flatMap { line =>
          line match {
            case LOG_PATTERN(json) => json :: Nil
            case _ =>
              println("unmatched log line: " + line)
              Seq.empty
          }
        }.write.mode(SaveMode.Overwrite).text(output)

      spark.stop()
  }
}
