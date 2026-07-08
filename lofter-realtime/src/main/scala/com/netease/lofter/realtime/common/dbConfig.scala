package com.netease.lofter.realtime.common

object dbConfig {
  val DRIVER = "com.netease.lbd.LBDriver"

  val LOFTER_DB_URL = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql:ddb://10.59.186.159:6000,10.59.186.155:6000,10.59.186.157:6000/public_online_gz?user=lofter_bi_gy_rw&password=5xcCUJ9@@&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")
  val LOFTER_MALL_DB_URL = sys.env.getOrElse("LOFTER_MALL_DB_URL", "jdbc:mysql:ddb://10.59.184.117:6000,10.59.184.122:6000,10.59.184.124:6000/mall_online_gz?user=lofter_bi_gy_rw&password=5xcCUJ9@@&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")
  val YAOLU_JDBC_URL = sys.env.getOrElse("YAOLU_DB_URL", "jdbc:mysql:ddb://10.59.186.122:6000,10.59.186.123:6000,10.59.186.124:6000/lofter_yaolu_online?connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf-8&user=lofter_bi_gy_rw&password=BEAl4@@v6")

  val mysqlDriver = "com.mysql.jdbc.Driver"
}
