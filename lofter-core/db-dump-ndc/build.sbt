name := "lofter-db-dump-ndc"
organization := "com.netease.yaolu"
version := "0.0.1"

connect in generateJob := {
  case "Risk_AuditPost" | "benefit_adTrace_config" =>
    "jdbc:mysql://10.59.186.164:6000/lofter-mirror-gz"

  case "AppFlyer_Push" | "ad_user_action_partition" | "ad_user_action_confirm" =>
    "jdbc:mysql://lofter-rds-statis-jd-34893.rds.cn-gz-p1.internal.:3306/comic_statis"

  case "conf_abtest" | "conf_multi_abtest" | "Conf_DispatchPlan" | "Conf_DispatchSource" | "Tc_Task" =>
    "jdbc:mysql://lofter-rds-common-recomment-mirror-gz-34729.rds.cn-gz-p1.internal.:3331/recomment"

  case table if table.startsWith("Ab_") =>
    "jdbc:mysql://lofter-rds-public-online-gz-34591.rds.cn-gz-p1.internal.:3331/dc_ab"

  case table if table.startsWith("MP_") || table.startsWith("AD_") || table == "DeviceCaid" =>
    "jdbc:mysql://10.59.186.122:6000/lofter-yaolu-online"

  case table if (table.startsWith("Dispatch_") && table != "Dispatch_ProjectApprovalOperator" ) || table.startsWith("Robot_") =>
    "jdbc:mysql://lofter-rds-flow-control-online-34888.rds.cn-gz-p1.internal.:3306/flow_control"

  case _ =>
    "jdbc:mysql://10.59.186.164:6000/lofter-mirror-gz"
}

connectUserName in generateJob := {
  case "AppFlyer_Push" | "ad_user_action_partition" | "ad_user_action_confirm" => "lofter_bi"
  case _ => "lofter_bi_gy"
}

connectPassword in generateJob := {
  case "Risk_AuditPost" | "benefit_adTrace_config" => "Q8@BJ5wh_"
  case "AppFlyer_Push" | "ad_user_action_partition" | "ad_user_action_confirm" => "qKsbCbRpM"
  case "conf_abtest" | "conf_multi_abtest" | "Conf_DispatchPlan" | "Conf_DispatchSource" | "Tc_Task" => "q4W0Kf_@I"
  case table if table.startsWith("Ab_") => "@2X1oKN_h"
  case table if table.startsWith("MP_") || table.startsWith("AD_") || table == "DeviceCaid"=> "w4W9F_A@q"
  case table if (table.startsWith("Dispatch_") && table != "Dispatch_ProjectApprovalOperator" ) || table.startsWith("Robot_") => "WjQ3@hE@1"
  case _ => "Q8@BJ5wh_"
}

val tableSet = Set("SharePost", "comment_hot", "PhotoPost", "PostHot", "TextPost", "UserFollowing", "Trade_UserFreeGift", "RecommendPostReviewLog", "User_CloseAccountDataBak",
  "Risk_AntispamResponse", "Risk_AntispamPost_TMP2", "Risk_AntispamCallbackRecord", "Risk_AntispamPostImage", "Trade_SupportRecord",
  "PVE_UserDialoguePartition", "Trade_GiftPresentRecord", "PVE_UserMaleVirtuePrisonLog", "Message")

connectMode in generateJob := {
  case tableName if tableSet(tableName) => "ndc"
  case _ => "qs"
}

tableFilter in generateJob := {
  case tableName if tableSet(tableName) => true
  case _ => false
}


jobOutputBaseDirectory in generateJob := "/user/da_lofter/db_dump"
jobOutputDirectoryMapper in generateJob := { table =>
  if(table == "Act_LighthouseUser") {
    s"Act_LighthouseUser_v2/$${azkaban.flow.1.days.ago}"
  } else s"$table/$${azkaban.flow.1.days.ago}"
}

tableMetaPath in generateJob := { baseDirectory.value / "meta.txt" }

lowPriorTables in generateJob := Seq()

highPriorTables in generateJob := Seq(
  "PostHot", "UserFollowing", "Trade_GiftPresentRecord", "PVE_UserDialoguePartition", "Trade_UserFreeGift", "Trade_SupportRecord", "TextPost", "comment_hot", "PhotoPost"
)

tableGroupSize in generateJob := 5

hiveSchema in generateJob := "lofter_db_dump"
hiveTableMapper in generateJob := {
  case "Act_LighthouseUser" => s"ods_db_act_lighthouse_user_v2_nd"
  case table => s"ods_db_${snakify(table)}_nd"
}

binlogTableMapper in generateJob := {
  case table => s"lofter.ods_binlog_${snakify(table)}_di"
}
