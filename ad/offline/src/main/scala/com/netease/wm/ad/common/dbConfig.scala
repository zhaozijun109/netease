package com.netease.wm.ad.common

object dbConfig {
  val lofterActivityJdbcUrl = sys.env.getOrElse("LOFTER_ACTIVITY_DB_URL", "jdbc:mysql://lofter-rds-activity-online-jd-34731.rds.cn-gz-p1.internal.:3306/lofter_activity?user=lofter_bi_gy&password=NO@b7Q_a9")
  val yaoluOnlineJdbcUrl = sys.env.getOrElse("YAOLU_ONLINE_DB_URL", "jdbc:mysql://10.59.186.122:6000/lofter-yaolu-online?connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf-8&user=lofter_bi_gy&password=w4W9F_A@q")
}
