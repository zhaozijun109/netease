package com.netease.wm.ad.rt.common

object dbConfig {
  val yaoluJdbcUrl = sys.env.getOrElse("YAOLU_DB_URL", "jdbc:mysql:ddb://yaolu-online-jd-qs-2.ddb.cn-east-p1.internal.:6000,yaolu-online-jd-qs-3.ddb.cn-east-p1.internal.:6000,yaolu-online-jd-qs-1.ddb.cn-east-p1.internal.:6000/yaolu_online?connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf-8&user=lofter_bi&password=yHZNWIXYD")
}