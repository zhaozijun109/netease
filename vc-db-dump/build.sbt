name := "vc-db-dump"
organization := "com.netease.yaolu"
version := "0.0.1"
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

val ndcTables = Set(
  "vc_user_message_record"
)

connect in generateJob := {
  case table if table.startsWith("Ab_") =>
    "jdbc:mysql://lofter-rds-public-online-gz-34591.rds.cn-gz-p1.internal.:3331/dc_ab"

  case "ad_channel_config" =>
    "jdbc:mysql://lofter-rds-statis-jd-34893.rds.cn-gz-p1.internal.:3306/comic_statis?useUnicode=true&characterEncoding=UTF-8"

  case _  =>
    "jdbc:mysql://vcharacter-mysql-rw-online.db.gy.ntes:4331/vcharacter?characterEncoding=utf-8&useSSL=false&autoReconnect=true&useAffectedRows=true&useLegacyDatetimeCode=false&useTimezone=true&serverTimezone=Asia/Shanghai"

}

connectUserName in generateJob := {
  case table if table.startsWith("Ab_") => "lofter_bi_gy"
  case "ad_channel_config" => "lofter_bi"
  case _ => "online_algorithm_r_user"
}
connectPassword in generateJob := {
  case table if table.startsWith("Ab_") => "@2X1oKN_h"
  case "ad_channel_config" => "qKsbCbRpM"
  case _ => "_FN68@jDm"
}

connectMode in generateJob := {
  case _ => "rds"
}

jobOutputBaseDirectory in generateJob := "/user/virtual_character/db_dump"
jobOutputDirectoryMapper in generateJob := {
  case table => s"$table/$${azkaban.flow.1.days.ago}"
}

val blacklist = Set("vc_user_character_memory_record","vc_user_message_record_old20240520","vc_user_message_record", "vc_event_info", "vc_push_message_info",
  "vc_user_character_memory_record", "vc_user_character_memory_relation")

tableMetaPath in generateJob := { baseDirectory.value / "meta.txt" }
tableFilter in generateJob := {
  case tableName if blacklist.contains(tableName) => false
  case tableName if tableName.startsWith("bak20241216_") || tableName.startsWith("droped_") || tableName.endsWith("_old")=> false
  case table if table.startsWith("Ab_") => true
  case tableName if tableName.startsWith("vc_") || tableName == "ad_channel_config" => true
  case _ => false
}

hiveSchema in generateJob := "vc"
hiveTableMapper in generateJob := {
  case table => s"ods_db_${snakify(table)}_nd"
}

binlogTableMapper in generateJob := {
  case table => s"vc.ods_binlog_${snakify(table)}_di"
}

