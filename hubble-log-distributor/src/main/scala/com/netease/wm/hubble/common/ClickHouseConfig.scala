package com.netease.wm.hubble.common

object ClickHouseConfig {
  val phyClickHouseJdbcUrl = "jdbc:clickhouse://lofter-data-common5.gy.ntes:9000,lofter-data-common6.gy.ntes:9000,lofter-data-common7.gy.ntes:9000,lofter-data-common8.gy.ntes:9000,lofter-data-common9.gy.ntes:9000/lofter?socket_timeout=1000000"
  val phyClickHouseUser = "lofter_rw"
  val phyClickHousePassword = "O4nWNA9slAn8"
  val clickHouseDriver = "com.github.housepower.jdbc.ClickHouseDriver"
}
