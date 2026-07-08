import sbt.util

organization := "com.netease.wm"

name := "hubble-log-distributor"

val kafkaVersion = "1.1.0"
val flinkVersion = "1.14.0"
val flinkKafkaConnectorVersion = "1.14.3"
val avroSchemaRegistryVersion = "0.1.22"

version := "0.3.0"
scalaVersion := "2.12.10"
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

logLevel in assembly := util.Level.Info

libraryDependencies ++= Seq(
  "com.github.housepower" % "clickhouse-native-jdbc" % "2.5.6",
  "com.netease.wm" %% "flink-avro-schema-registry" % avroSchemaRegistryVersion,
  "com.netease.wm" %% "wm-util" % "0.2.0",
  "com.alibaba" % "druid" % "1.1.16",
  "mysql" % "mysql-connector-java" % "5.1.20",
  "com.github.blemale" %% "scaffeine" % "3.1.0",
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "com.netease.backend" % "dts-sdk" % "3.9.0" exclude("xmlpull", "xmlpull"),
  "org.json4s" %% "json4s-core" % "3.6.9",
  "org.json4s" %% "json4s-jackson" % "3.6.9",
  "org.json4s" %% "json4s-native" % "3.6.9",
  "org.apache.avro" % "avro" % "1.10.1",
  "com.github.mjakubowski84" %% "parquet4s-core" % "1.0.0",
  "com.hadoop.gplcompression" % "hadoop-lzo" % "0.4.20",
  "com.github.nscala-time" %% "nscala-time" % "2.14.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.6",
  "org.elasticsearch" % "elasticsearch" % "6.8.6",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "6.8.6",
  "org.apache.parquet" % "parquet-avro" % "1.12.2",
  "com.ververica" % "flink-connector-mysql-cdc" % "2.4.1" exclude("org.apache.flink", "flink-shaded-guava") exclude("org.apache.kafka", "kafka-clients"),
  "org.apache.flink" %% "flink-parquet" % flinkVersion,
  "org.apache.flink" % "flink-avro" % flinkVersion,
  "org.apache.flink" %% "flink-state-processor-api" % flinkVersion,
  "org.apache.flink" % "flink-connector-files" % flinkVersion,
  "org.apache.flink" %% "flink-connector-elasticsearch6" % flinkVersion,
  "org.apache.flink" %% "flink-connector-kafka" % flinkKafkaConnectorVersion excludeAll("org.apache.flink"),
  "org.apache.flink" %% "flink-connector-jdbc" % flinkVersion,
  "org.apache.flink" %% "flink-hadoop-compatibility" % flinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.flink" %% "flink-table-planner" % flinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.flink" %% "flink-table-api-scala-bridge" % flinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.hadoop" % "hadoop-common" % "2.7.5" % "provided",
  "com.alibaba" % "fastjson" % "1.2.76"
)

assemblyShadeRules in assembly ++= Seq(
  ShadeRule.rename("org.apache.avro.**" -> "shaded.org.apache.avro.@1")
    .inLibrary("org.apache.avro" % "avro" % "1.10.1")
    .inLibrary("org.apache.parquet" % "parquet-avro" % "1.12.2")
    .inLibrary("org.apache.flink" % "flink-avro" % flinkVersion)
    .inLibrary("com.netease.wm" % "flink-avro-schema-registry_2.12" % avroSchemaRegistryVersion)
    .inProject,
  ShadeRule.rename("org.apache.parquet.avro.**" -> "shaded.org.apache.parquet.avro.@1")
    .inLibrary("org.apache.flink" % "flink-avro" % flinkVersion)
    .inLibrary("org.apache.parquet" % "parquet-avro" % "1.12.2")
    .inLibrary("com.netease.wm" % "flink-avro-schema-registry_2.12" % avroSchemaRegistryVersion)
    .inProject,
  ShadeRule.zap("org.slf4j.impl.**").inAll
)

assemblyExcludedJars in assembly := {
  (fullClasspath in assembly) map { cp =>
    cp filter { jar =>
      jar.data.getName.startsWith("log4j-") ||
        jar.data.getName.startsWith("flink-core") ||
        jar.data.getName.startsWith("flink-metrics") ||
        jar.data.getName.startsWith("slf4j-") ||
        jar.data.getName.startsWith("jcl-over-slf4j")
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
