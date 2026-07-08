name := "ad-realtime"
organization := "com.netease.yaolu"
version := "0.1.1"

val flinkVersion = "1.11.3"

libraryDependencies ++= Seq(
  "com.squareup.okhttp3" % "okhttp" % "4.2.0",
  "com.github.nscala-time" %% "nscala-time" % "2.14.0",
  "com.netease.backend" % "lbd" % "1.1.7" exclude("mysql", "mysql-connector-java"),
  "com.alibaba" % "druid" % "1.1.16",
  "com.google.guava" % "guava" % "11.0.2",
  "org.json4s" %% "json4s-core" % "3.2.11",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.netease.wm" %% "wm-util" % "0.2.0",
  "com.twitter" %% "algebird-core" % "0.12.4",
  "mysql" % "mysql-connector-java" % "5.1.20",
  "org.apache.kafka" % "kafka-clients" % "1.1.0",
  "org.apache.parquet" % "parquet-hadoop" % "1.10.1",
  "org.apache.flink" %% "flink-connector-kafka" % flinkVersion,
  "org.apache.flink" %% "flink-connector-jdbc" % flinkVersion,
  "org.apache.flink" %% "flink-hadoop-compatibility" % flinkVersion,
  "org.apache.flink" %% "flink-table-api-scala-bridge" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-table-planner" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided"
)

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("shapeless.**" -> "shadeshapeless.@1")
    .inLibrary("com.netease.wm" %% "wm-util" % "0.2.0")
    .inLibrary("com.chuusai" %% "shapeless" % "2.3.2")
    .inProject
)
