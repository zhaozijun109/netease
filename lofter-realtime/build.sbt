import sbt.Opts.resolver

name := "lofter-realtime"
organization := "com.netease.yaolu"
version := "0.0.1"
scalaVersion := "2.12.10"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint")

val flinkVersion = "1.20.0"
val flinkKafkaConnectorVersion = "1.20.0"
val avroSchemaRegistryVersion = "0.2.0"

libraryDependencies ++= Seq(
  "org.apache.doris" % "flink-doris-connector-1.20" % "25.1.0",
  "com.github.housepower" % "clickhouse-native-jdbc" % "2.5.6",
  "com.github.blemale" %% "scaffeine" % "3.1.0",
  "com.squareup.okhttp3" % "okhttp" % "3.14.9",
  "com.squareup.okio" % "okio" % "1.17.2",
  "com.netease.wm" %% "flink-avro-schema-registry" % avroSchemaRegistryVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % "2.11.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.11.3",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.11.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.3",
  "com.netease.wm" %% "wm-util" % "0.2.1",
  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.github.nscala-time" %% "nscala-time" % "2.26.0",
  "com.hadoop.gplcompression" % "hadoop-lzo" % "0.4.20",
  "org.apache.hadoop" % "hadoop-common" % "2.6.0" % "provided",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.6.0" % "provided",
  "com.netease.backend" % "dts-sdk" % "1.1.1" exclude("xmlpull", "xmlpull"),
  "com.netease.backend" % "lbd" % "1.1.7" exclude("mysql", "mysql-connector-java"),
  "mysql" % "mysql-connector-java" % "5.1.20",
  "com.alibaba" % "druid" % "1.1.16",
  "org.json4s" %% "json4s-core" % "3.2.11",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.6",
  "org.elasticsearch" % "elasticsearch" % "6.8.6",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "6.8.6",
  "org.apache.avro" % "avro" % "1.11.4",
  "com.github.mjakubowski84" %% "parquet4s-core" % "1.0.0",
  "org.apache.flink" % "flink-connector-jdbc" % "3.3.0-1.20",
  "org.apache.flink" % "flink-connector-kafka" % "3.3.0-1.20",
  "org.apache.flink" % "flink-json" % flinkVersion,
  "org.apache.flink" %% "flink-hadoop-compatibility" % flinkVersion,
  "org.apache.flink" % "flink-table-api-java-uber" % flinkVersion,
  "org.apache.flink" % "flink-table-runtime" % flinkVersion % "provided",
  "org.apache.flink" % "flink-table-planner-loader" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-table-api-scala-bridge" % flinkVersion,
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided"
)

resolvers ++= Seq(
  // ("comic-releases" at "http://10.172.113.214:8081/content/repositories/releases").withAllowInsecureProtocol(true),
  ("netease" at "http://mvn.hz.netease.com/artifactory/repo/").withAllowInsecureProtocol(true)
)

assemblyShadeRules in assembly ++= Seq(
  ShadeRule.rename("org.apache.avro.**" -> "shaded.org.apache.avro.@1")
    .inLibrary("org.apache.avro" % "avro" % "1.11.4")
    .inLibrary("org.apache.flink" % "flink-avro" % flinkVersion)
    .inLibrary("com.netease.wm" % "flink-avro-schema-registry_2.12" % avroSchemaRegistryVersion)
    .inProject,
  ShadeRule.rename("okhttp3.**" -> "shaded.okhttp3.@1")
    .inLibrary("com.squareup.okhttp3" % "okhttp" % "3.14.9")
    .inProject,
  ShadeRule.rename("okio.**" -> "shaded.okio.@1")
    .inLibrary("com.squareup.okio" % "okio" % "1.17.2")
    .inLibrary("com.squareup.okhttp3" % "okhttp" % "3.14.9")
    .inProject
)

assemblyExcludedJars in assembly := {
  (fullClasspath in assembly) map { cp =>
    cp filter { jar =>
      jar.data.getName.startsWith("log4j-") ||
        jar.data.getName.startsWith("flink-core") ||
        jar.data.getName.startsWith("flink-metrics")
    }
  }
}.value

autoScalaLibrary := true

crossPaths := false

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF", xs @ _* ) => MergeStrategy.discard
  case PathList("META-INF", "services", "org.apache.flink.table.factories.Factory", xs @ _* ) => MergeStrategy.concat
  case x => MergeStrategy.first
}

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
