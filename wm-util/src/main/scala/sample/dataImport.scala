package sample

import scala.io.Codec

object dataImport {
  def main(args: Array[String]): Unit = {
    import com.netease.wm.util.Sql._
    import java.sql.{Connection, DriverManager}

    def getDbConn: Connection = {
      Class.forName("com.mysql.jdbc.Driver")
      DriverManager.getConnection(sys.env.getOrElse("YOUDATA_DB_URL", "jdbc:mysql://10.201.241.27:3306/yaolu_rds?user=yaolu_static&password=roNaCAmJ{"))
    }

    implicit val gbkCodec: Codec = Codec("GBK")

    val localDb: ThreadLocal[Connection] = new ThreadLocal[Connection]{
      override def initialValue(): Connection = {
        val conn = getDbConn
        conn.setAutoCommit(false)
        conn
      }
    }

    val batchSize = 1000

    for(
      batch <- scala.io.Source.fromFile("/tmp/0627.csv").getLines.drop(1).grouped(batchSize)
    ) {
      batch.map{ line =>
        val p = line.substring(1, line.length - 1).split("\",\"")
        param(p(1), p(2).toDouble, p(3), p(4), p(5), p(6), p(7), p(8), p(9), p(10), p(11), p(12), p(13), p(14), p(15), p(16), p(17).toDouble, p.lift(18).getOrElse(""))
      }.grouped(batchSize)
        .toSeq
        .foreach { rows =>
          implicit val db: Connection = localDb.get()
          sql"""
               | insert into snail_stat_result2(DATEID, REPORTID, LEVELA, LEVELB, LEVELC, LEVELD, LEVELE, LEVELF, LEVELG,
               |                               LEVELH, LEVELI, LEVELJ, LEVELK, LEVELL, LEVELM, LEVELN, VALUEX, NAME)
               |  values(str_to_date(${0},'%Y/%m/%d'), ${1},${2}, ${3}, ${4}, ${5}, ${6}, ${7}, ${8}, ${9}, ${10}, ${11}, ${12}, ${13}, ${14}, ${15}, ${16}, ${17})
          """.stripMargin.batchUpdate(rows)

          db.commit()
        }
    }
  }
}
