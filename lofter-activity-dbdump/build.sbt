name := "lofter-activity-db-dump"
organization := "com.netease.yaolu"
version := "0.0.1"
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

connect in generateJob := {
  case _  => "jdbc:mysql://lofter-rds-activity-online-jd-34731.rds.cn-gz-p1.internal.:3306/lofter_activity"
}

connectUserName in generateJob := {
  case _ => "lofter_bi_gy"
}
connectPassword in generateJob := {
  case _ => "NO@b7Q_a9"
}

connectMode in generateJob := {
  case _ => "rds"
}

jobOutputBaseDirectory in generateJob := "/user/da_lofter/db_dump"
jobOutputDirectoryMapper in generateJob := { 
  case table if table.toLowerCase.startsWith("act_") => table
  case table => s"act_$table/$${azkaban.flow.1.days.ago}"
}

tableMetaPath in generateJob := { baseDirectory.value / "meta.txt" }
tableFilter in generateJob := {
    case "Luck_Bingo" | "Luck_Chance" | "Act_AnnualReport" | "backend_chuizi_paper" | "Carnival_SweetContent" | "act__Act_Event_RankRecord_drop20230815" | "_Pixel_WriteLog_old" => false
    case table if table.startsWith("droped_") || table.startsWith("_OWL_AUTO_OFFLINE_") => false
    case _ => true
}

highPriorTables in generateJob := Seq(
  "Activity_Password", "Act_Event", "Act_LighthouseUser", "Act_Project", "Act_ProjectActivityRecord",
  "Act_UserActivityRecord", "Activity_EffectBaseConfig", "Incentive_Mission", "KeFu_FeedBack", "Incentive_MissionUser",
  "Incentive_MissionUserProgress", "Reward_UserPayInfo", "adspace_snapshot", "backend_message_push_record"
)

tableGroupSize in generateJob := 5

hiveSchema in generateJob := "lofter_db_dump"
hiveTableMapper in generateJob := { 
  case table if table.toLowerCase.startsWith("act_") => s"ods_db_${snakify(table)}_nd"
  case table => s"ods_db_act_${snakify(table)}_nd"
}

