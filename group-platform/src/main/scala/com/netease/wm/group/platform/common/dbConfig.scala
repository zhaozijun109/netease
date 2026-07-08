package com.netease.wm.group.platform.common

object dbConfig {
  val DRIVER: String = "com.mysql.jdbc.Driver"
  val LBDRIVER: String = "com.netease.lbd.LBDriver"

//  val lofterDDBOnLineUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql:ddb://lofter-online-jd-qs-6.ddb.cn-east-p1.internal.:6000,lofter-online-jd-qs-7.ddb.cn-east-p1.internal.:6000,lofter-online-jd-qs-8.ddb.cn-east-p1.internal.:6000/public_online?user=lofter_bi&password=WndkIpgkr&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")
  val lofterDDBOnLineUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql:ddb://10.59.186.159:6000,10.59.186.155:6000,10.59.186.157:6000/public_online_gz?user=lofter_bi_gy_rw&password=5xcCUJ9@@&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")

  val lofterDDBTestUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql:ddb://10.57.60.11:6000/public?user=public_test&password=eThNXWMVirg&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")

  val mallOnLineUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql:ddb://lofter-online-jd-qs-21.ddb.cn-east-p1.internal.:6000,lofter-online-jd-qs-22.ddb.cn-east-p1.internal.:6000,lofter-online-jd-qs-23.ddb.cn-east-p1.internal.:6000/mall_online?user=lofter_bi&password=WndkIpgkr&characterEncoding=utf8&connectTimeout=5000&socketTimeout=1800000")

  val lofterRecFlowOnLineUrl = sys.env.getOrElse("LOFTER_DB_URL", "jdbc:mysql://lofter-rds-flow-control-online-28633.rds.cn-east-p1.internal.:3331/flow_control?user=lofter_bi&password=ZhQFerjNT&connectTimeout=5000&socketTimeout=1800000&characterEncoding=utf8")
  val recDDBUrl = "jdbc:mysql://lofter-rds-common-recomment-online-jd-27164.rds.cn-east-p1.internal.:3331/recomment?useUnicode=true&characterEncoding=UTF-8&user=lofter_bi&password=mBESHdBRr"
  val recDDBTestUrl = "jdbc:mysql://10.172.117.52:3331/recommend-db?user=lofter_activity&password=yFtVAuPAP&useUnicode=true&characterEncoding=utf-8"
}
