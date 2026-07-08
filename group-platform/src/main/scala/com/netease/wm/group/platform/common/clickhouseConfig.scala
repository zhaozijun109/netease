package com.netease.wm.group.platform.common

object clickhouseConfig {
  val hiveUrl = "jdbc:hive2://10.189.179.235:10001/;principal=da_lofter/dev@HADOOP.HZ.NETEASE.COM"
  val hiveDatabase = "lofter_dm"
  val clickHousePrefix = "jdbc:clickhouse://"
  val clickHouseSuffix = "/hive?socket_timeout=1000000"
  val clickHouseHost = List("lofter-data-common5.gy.ntes","lofter-data-common6.gy.ntes","lofter-data-common7.gy.ntes","lofter-data-common8.gy.ntes","lofter-data-common9.gy.ntes")
  val clickHouseTableHost = "lofter-data-common7.gy.ntes"
  val clickHouseViewHost = List("lofter-data-common5.gy.ntes","lofter-data-common6.gy.ntes")
  val clickHousePort = ":8123"
  val clickHousePassword = "O4nWNA9slAn8"
  val clickHouseUser = "lofter_rw"
  val clickHouseDriverName = "com.clickhouse.jdbc.ClickHouseDriver"
  val batchSize = 10000
  val parallelism = 10

  def getMasterClickhouseJdbcUrl: String = clickHousePrefix + clickHouseTableHost + clickHousePort + clickHouseSuffix
  def getSlaveClickhouseJdbcUrl: Seq[String] = clickHouseViewHost.map(host => clickHousePrefix + host + clickHousePort + clickHouseSuffix)
}
