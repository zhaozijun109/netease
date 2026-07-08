package com.netease.vc.data.common

object clickhouseConfig {
  val hiveUrl = "jdbc:hive2://10.59.187.159:10001/;principal=da_lofter/dev@HADOOP.HZ.NETEASE.COM"
  val hiveDatabase = "vc"
  val clickHousePrefix = "jdbc:clickhouse://"
  val clickHouseSuffix = "/hive?socket_timeout=1000000"
  val clickHouseHost = List("lofter-data-common5.gy.ntes","lofter-data-common6.gy.ntes","lofter-data-common7.gy.ntes","lofter-data-common8.gy.ntes","lofter-data-common9.gy.ntes")
  val clickHouseTableHost = "lofter-data-common7.gy.ntes"
  val clickHouseViewHost = List("lofter-data-common5.gy.ntes","lofter-data-common6.gy.ntes","lofter-data-common8.gy.ntes","lofter-data-common9.gy.ntes")
  val clickHousePort = ":8123"
  val clickHousePassword = "O4nWNA9slAn8"
  val clickHouseUser = "lofter_rw"
  val clickHouseDriverName = "com.clickhouse.jdbc.ClickHouseDriver"
  val batchSize = 10000
  val parallelism = 10

  def getMasterClickhouseJdbcUrl: String = clickHousePrefix + clickHouseTableHost + clickHousePort + clickHouseSuffix
  def getSlaveClickhouseJdbcUrl: Seq[String] = clickHouseViewHost.map(host => clickHousePrefix + host + clickHousePort + clickHouseSuffix)

  val historyLoadJobs: Set[String] = Set("ads_pve_app_chat_retain_data_di","ads_pve_app_chat_base_data_di","ads_vc_ad_cvr_di","ads_vc_ad_cvr_new_di",
    "ads_appversion_device_dd","ads_deviceosversion_device_dd","ads_vc_growth_ad_period_cvr_di")
}
