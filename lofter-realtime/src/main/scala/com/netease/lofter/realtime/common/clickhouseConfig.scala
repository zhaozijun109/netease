package com.netease.lofter.realtime.common

object clickhouseConfig {
  val phyClickHouseJdbcUrl = "jdbc:clickhouse://lofter-data-common5.gy.ntes:9000/lofter?socket_timeout=1000000"
  val phyClickHouseUser = "lofter_rw"
  val phyClickHousePassword = "O4nWNA9slAn8"
  val clickHouseDriver = "com.github.housepower.jdbc.ClickHouseDriver"
}
