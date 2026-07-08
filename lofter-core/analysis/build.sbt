name := "lofter-analysis"

libraryDependencies ++= Seq(
  "com.huaban" % "jieba-analysis" % "1.0.2",
  "com.netease.cloud" % "nos-sdk-java" % "1.2.6",
  "org.json4s" %% "json4s-core" % "3.2.11",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.clickhouse" % "clickhouse-jdbc" % "0.4.1" classifier "http",
  "com.clickhouse" % "clickhouse-data" % "0.4.1",
  "org.roaringbitmap" % "RoaringBitmap" % "0.9.39",
  "mysql" % "mysql-connector-java" % "5.1.20",
  "com.alibaba" % "druid" % "1.1.16",
  "com.alibaba" % "fastjson" % "1.2.76",
  "com.netease.backend" % "lbd" % "1.1.7" exclude("mysql", "mysql-connector-java"),
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "com.twitter" %% "algebird-core" % "0.12.4",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "7.17.28",
  "org.elasticsearch" %% "elasticsearch-spark-30" % "7.17.28",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.3.2",
  "org.apache.spark" %% "spark-mllib" % "3.3.2" % "provided",
  "org.apache.spark" %% "spark-core" % "3.3.2" % "provided",
  "org.apache.spark" %% "spark-sql" % "3.3.2" % "provided",
  "org.apache.hadoop" % "hadoop-common" % "2.10.2" % "provided",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.10.2" % "provided",
  "org.apache.hadoop" % "hadoop-common" % "2.10.2" % "provided",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.10.2" % "provided",
  "redis.clients" % "jedis" % "5.1.5"
)

assemblyExcludedJars in assembly := {
  (fullClasspath in assembly) map { cp =>
    val excludes = Set(
      "hadoop-common-2.10.2.jar",
      "hadoop-annotations-2.10.2.jar",
      "hadoop-auth-2.10.2.jar",
      "hadoop-client-runtime-3.3.2.jar",
      "hadoop-client-api-3.3.2.jar"
    )
    cp filter { jar => excludes(jar.data.getName) }
  }
}.value

assemblyShadeRules in assembly ++= Seq(
  ShadeRule.rename("shapeless.**" -> "shaded.shapeless.@1")
    .inLibrary("com.netease.wm" % "wm-util_2.12" % "0.2.2")
    .inLibrary("com.chuusai" % "shapeless_2.12" % "2.3.3")
    .inProject,
  ShadeRule.rename("org.roaringbitmap.**" -> "shaded.org.roaringbitmap.@1")
    .inLibrary("org.roaringbitmap" % "RoaringBitmap" % "0.9.39")
    .inLibrary("com.clickhouse" % "clickhouse-jdbc" % "0.4.1" classifier "http")
    .inLibrary("com.clickhouse" % "clickhouse-data" % "0.4.1")
    .inProject
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF", xs @ _* ) => MergeStrategy.discard
  case PathList("META-INF", "services", "org.apache.spark.sql.sources.DataSourceRegister", xs @ _* ) => MergeStrategy.concat
  case x => MergeStrategy.first
}
