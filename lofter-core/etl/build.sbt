name := "lofter-etl"

libraryDependencies ++= Seq(
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "com.github.mjakubowski84" %% "parquet4s-core" % "1.0.0",
  "com.huaban" % "jieba-analysis" % "1.0.2",
  "com.clickhouse" % "clickhouse-jdbc" % "0.4.1" classifier "http",
  "com.clickhouse" % "clickhouse-data" % "0.4.1",
  "org.roaringbitmap" % "RoaringBitmap" % "0.9.39",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.8.11",
  "com.alibaba" % "fastjson" % "1.2.76",
  "mysql" % "mysql-connector-java" % "5.1.20",
  "com.alibaba" % "druid" % "1.1.16",
  "com.netease.backend" % "lbd" % "1.1.7" exclude("mysql", "mysql-connector-java"),
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "com.twitter" %% "algebird-core" % "0.12.4",
  "org.elasticsearch" %% "elasticsearch-spark-30" % "7.12.0",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.3.2",
  "org.apache.spark" %% "spark-mllib" % "3.3.2" % "provided",
  "org.apache.spark" %% "spark-core" % "3.3.2" % "provided",
  "org.apache.spark" %% "spark-sql" % "3.3.2" % "provided",
  "org.apache.hadoop" % "hadoop-common" % "2.10.2" % "provided",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.10.2" % "provided",
  "org.apache.hadoop" % "hadoop-common" % "2.10.2" % "provided",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.10.2" % "provided",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "org.apache.spark" %% "spark-core" % "3.3.2" % "test",
  "org.apache.spark" %% "spark-sql" % "3.3.2" % "test",
  "org.apache.spark" %% "spark-hive" % "3.3.2" % "test",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.5" % "test",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.13.5" % "test",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.5" % "test"
)

Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

Test / fork := true
Test / javaOptions ++= Seq(
  "-Xmx2g",
  s"-Djava.security.policy=${(Test / baseDirectory).value}/src/test/resources/test.policy",
  "-Dderby.stream.error.file=/dev/null"
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
    .inLibrary("com.github.mjakubowski84" % "parquet4s-core_2.12" % "1.0.0")
    .inProject,
  ShadeRule.rename("org.roaringbitmap.**" -> "shaded.org.roaringbitmap.@1")
    .inLibrary("org.roaringbitmap" % "RoaringBitmap" % "0.9.39")
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
