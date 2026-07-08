name := "vc-data"
organization := "com.netease.yaolu"
version := "0.1.0"
scalaVersion := "2.12.10"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint")

libraryDependencies ++= Seq(
  "com.netease.wm" %% "wm-util" % "0.2.1",
  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.github.nscala-time" %% "nscala-time" % "2.26.0",
  "com.hadoop.gplcompression" % "hadoop-lzo" % "0.4.20",
  "org.json4s" %% "json4s-core" % "3.2.11",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.clickhouse.spark" %% "clickhouse-spark-runtime-3.3" % "0.8.1",
//  "com.clickhouse" % "clickhouse-jdbc" % "0.6.3" classifier "all",
//  "com.clickhouse" % "clickhouse-data" % "0.6.3",
  "com.clickhouse" % "clickhouse-jdbc" % "0.4.1" classifier "http",
  "com.clickhouse" % "clickhouse-data" % "0.4.1",
  "org.roaringbitmap" % "RoaringBitmap" % "0.9.39",
  "mysql" % "mysql-connector-java" % "5.1.20",
  "com.alibaba" % "druid" % "1.1.16",
  "com.alibaba" % "fastjson" % "1.2.76",
  "com.netease.backend" % "lbd" % "1.1.7" exclude("mysql", "mysql-connector-java"),
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "com.twitter" %% "algebird-core" % "0.12.4",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.3.0",
  "org.elasticsearch" %% "elasticsearch-spark-30" % "8.13.4",
  "org.elasticsearch" % "elasticsearch" % "7.17.28",
  "org.elasticsearch.client" % "elasticsearch-rest-client" % "8.13.4",
  "co.elastic.clients" % "elasticsearch-java" % "8.13.4",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.5",
  "org.apache.spark" %% "spark-core" % "3.3.0" % "provided",
  "com.alibaba.fastjson2" % "fastjson2" % "2.0.53",
  "org.apache.spark" %% "spark-sql" % "3.3.0" % "provided",
  "org.apache.hadoop" % "hadoop-common" % "2.9.2" % "provided",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.9.2" % "provided",
  "org.apache.hadoop" % "hadoop-common" % "2.9.2" % "provided",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.9.2" % "provided",
  "redis.clients" % "jedis" % "3.5.2"
)

assemblyExcludedJars in assembly := {
  (fullClasspath in assembly) map { cp =>
    val excludes = Set(
      "hadoop-common-2.9.2.jar",
      "hadoop-annotations-2.9.2.jar",
      "hadoop-auth-2.9.2.jar",
      "hadoop-client-runtime-3.3.2.jar",
      "hadoop-client-api-3.3.2.jar"
    )
    cp filter { jar => excludes(jar.data.getName) }
  }
}.value

assemblyShadeRules in assembly ++= Seq(
  ShadeRule.rename("shapeless.**" -> "shaded.shapeless.@1")
    .inLibrary("com.netease.wm" % "wm-util_2.12" % "0.2.1")
    .inLibrary("com.chuusai" % "shapeless_2.12" % "2.3.3")
    .inLibrary("com.github.mjakubowski84" % "parquet4s-core_2.12" % "1.0.0")
    .inProject,
  ShadeRule.rename("org.roaringbitmap.**" -> "shaded.org.roaringbitmap.@1")
    .inLibrary("org.roaringbitmap" % "RoaringBitmap" % "1.2.1")
    .inLibrary("com.clickhouse" % "clickhouse-jdbc" % "0.4.1" classifier "http")
    .inLibrary("com.clickhouse" % "clickhouse-data" % "0.4.1")
    .inProject,
  ShadeRule.rename("com.github.mjakubowski84.**" -> "shaded.com.github.mjakubowski84.@1")
   .inLibrary("com.github.mjakubowski84" % "parquet4s-core_2.12" % "1.0.0")
    .inProject
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF", xs @ _* ) => MergeStrategy.discard
  case PathList("META-INF", "services", "org.apache.spark.sql.sources.DataSourceRegister", xs @ _* ) => MergeStrategy.concat
  case x => MergeStrategy.first
}

