package com.netease.lofter.etl.common

object dbConfig {

  val DRIVER: String = "com.mysql.jdbc.Driver"
  val LBDRIVER: String = "com.netease.lbd.LBDriver"

  val lofterDDBOnLineUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql:ddb://10.59.186.159:6000,10.59.186.155:6000,10.59.186.157:6000/public_online_gz?user=lofter_bi_gy_rw&password=5xcCUJ9@@&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")

  val mallOnLineUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql:ddb://10.59.184.117:6000,10.59.184.122:6000,10.59.184.124:6000/mall_online_gz?user=lofter_bi_gy_rw&password=5xcCUJ9@@&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")

  val lofterRecFlowOnLineUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql://lofter-rds-flow-control-online-34888.rds.cn-gz-p1.internal.:3306/flow_control?user=lofter_bi_gy_rw&password=PI_49@Rvt&connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf8")
}
