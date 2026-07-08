organization := "com.netease.wm"

name := "parsed-rec-data"

val kafkaVersion = "1.1.0"
val flinkVersion = "1.14.0"
val flinkKafkaConnectorVersion = "1.14.3"
val avroSchemaRegistryVersion = "0.1.32"
val rsVersion = "2.2.2"
val newFlinkVersion = "1.20.3"
val newFlinkKafkaConnectorVersion = "3.3.0-1.20"
val newFlinkJdbcConnectorVersion = "3.3.0-1.20"
val newFlinkEs6ConnectorVersion = "3.1.0-1.20"

version := "0.2.0"
scalaVersion := "2.12.10"
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

libraryDependencies ++= Seq(
//  "ch.qos.logback" % "logback-classic" % "1.2.12",   // 本地调试
  "com.netease.wm" %% "flink-avro-schema-registry" % avroSchemaRegistryVersion,
  "com.netease.wm" %% "wm-util" % "0.2.1",
  "com.netease.yaolu.lofter" % "rs-basic-upload-parse" % rsVersion,
  "com.netease.yaolu.lofter" % "rs-basic-util" % rsVersion,
  "com.netease.yaolu.lofter" % "rs-basic-bean" % rsVersion,
  "com.alibaba" % "druid" % "1.1.16",
  "mysql" % "mysql-connector-java" % "5.1.20",
  "com.github.blemale" %% "scaffeine" % "3.1.0",
  "com.netease.backend" % "dts-sdk" % "1.1.1" exclude("xmlpull", "xmlpull"),
  "org.json4s" %% "json4s-core" % "3.6.9",
  "org.json4s" %% "json4s-jackson" % "3.6.9",
  "com.hadoop.gplcompression" % "hadoop-lzo" % "0.4.20",
  "com.github.nscala-time" %% "nscala-time" % "2.14.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.15.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.2",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.15.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2",
  "org.elasticsearch" % "elasticsearch" % "6.8.6",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "6.8.6",
  "org.apache.avro" % "avro" % "1.11.4",
  "org.apache.parquet" % "parquet-avro" % "1.15.2" exclude("org.apache.avro", "avro"),
  "org.apache.flink" % "flink-parquet" % newFlinkVersion,
  "org.apache.flink" % "flink-avro" % newFlinkVersion exclude("org.apache.avro", "avro"),
  "org.apache.flink" % "flink-clients" % newFlinkVersion % "provided",
  "org.apache.flink" % "flink-statebackend-rocksdb" % newFlinkVersion,
  "org.apache.flink" % "flink-state-processor-api" % newFlinkVersion,
  "org.apache.flink" % "flink-connector-base" % newFlinkVersion,
  "org.apache.flink" % "flink-connector-files" % newFlinkVersion,
  "org.apache.flink" % "flink-connector-elasticsearch6" % newFlinkEs6ConnectorVersion,
  "org.apache.flink" % "flink-connector-kafka" % newFlinkKafkaConnectorVersion,
  "org.apache.flink" % "flink-connector-jdbc" % newFlinkJdbcConnectorVersion,
  "org.apache.flink" %% "flink-hadoop-compatibility" % newFlinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.flink" % "flink-table-api-java-uber" % newFlinkVersion,
  "org.apache.flink" % "flink-table-runtime" % newFlinkVersion,
  "org.apache.flink" % "flink-table-planner-loader" % newFlinkVersion,
  "org.apache.flink" %% "flink-table-api-scala-bridge" % newFlinkVersion,
  "org.apache.flink" %% "flink-scala" % newFlinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.flink" %% "flink-streaming-scala" % newFlinkVersion % "provided" excludeAll("org.scala-lang.modules"),
  "org.apache.hadoop" % "hadoop-common" % "2.7.5" % "provided"
)

resolvers += Resolver.mavenLocal

assemblyExcludedJars in assembly := {
  (fullClasspath in assembly) map { cp =>
    cp filter { jar => jar.data.getName.startsWith("log4j-") }
  }
}.value

assemblyShadeRules in assembly ++= Seq(
  ShadeRule.rename("org.apache.avro.**" -> "shaded.org.apache.avro.@1")
    .inLibrary("org.apache.avro" % "avro" % "1.11.4")
    .inLibrary("org.apache.flink" % "flink-avro" % flinkVersion)
    .inLibrary("com.netease.wm" % "flink-avro-schema-registry_2.12" % avroSchemaRegistryVersion)
    .inProject,
  ShadeRule.rename("okhttp3.**" -> "shaded.okhttp3.@1")
    .inLibrary("com.squareup.okhttp3" % "okhttp" % "3.14.9")
    .inLibrary("com.netease.yaolu.lofter" % "rs-basic-upload-parse" % rsVersion)
    .inLibrary("com.netease.yaolu.lofter" % "rs-basic-util" % rsVersion)
    .inLibrary("com.netease.yaolu.lofter" % "rs-basic-bean" % rsVersion)
    .inProject,
  ShadeRule.rename("okio.**" -> "shaded.okio.@1")
    .inLibrary("com.squareup.okio" % "okio" % "1.17.2")
    .inLibrary("com.squareup.okhttp3" % "okhttp" % "3.14.9")
    .inLibrary("com.netease.yaolu.lofter" % "rs-basic-upload-parse" % rsVersion)
    .inLibrary("com.netease.yaolu.lofter" % "rs-basic-util" % rsVersion)
    .inLibrary("com.netease.yaolu.lofter" % "rs-basic-bean" % rsVersion)
    .inProject
)

assemblyExcludedJars in assembly := {
  (fullClasspath in assembly) map { cp =>
    cp filter { jar =>
      jar.data.getName.startsWith("log4j-")
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
