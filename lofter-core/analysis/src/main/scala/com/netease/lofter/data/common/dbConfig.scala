package com.netease.lofter.data.common

object dbConfig {

  val DRIVER: String = "com.mysql.jdbc.Driver"
  val LBDRIVER: String = "com.netease.lbd.LBDriver"

  val lofterDDBOnLineUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql:ddb://10.59.186.159:6000,10.59.186.155:6000,10.59.186.157:6000/public_online_gz?user=lofter_bi_gy_rw&password=5xcCUJ9@@&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")

  val mallOnLineUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql:ddb://10.59.184.117:6000,10.59.184.122:6000,10.59.184.124:6000/mall_online_gz?user=lofter_bi_gy_rw&password=5xcCUJ9@@&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")

  val recDDBUrl = "jdbc:mysql://lofter-rds-common-recomment-online-gz-34726.rds.cn-gz-p1.internal.:3331/recomment?useUnicode=true&characterEncoding=UTF-8&user=lofter_bi_gy&password=q4W0Kf_@I"
}
