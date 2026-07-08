name := "ad-offline"
organization := "com.netease.wm"
version := "0.1.1"

val sparkVersion = "3.3.2"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-core" % "3.2.11",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.squareup.okhttp3" % "okhttp" % "3.10.0",
  "com.squareup.okhttp3" % "logging-interceptor" % "3.10.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.8.11",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.11",
  "com.netease.wm" %% "wm-util" % "0.2.0",
  "com.github.nscala-time" %% "nscala-time" % "2.14.0",
  "mysql" % "mysql-connector-java" % "5.1.35",
  "org.apache.spark" %% "spark-core" % sparkVersion  % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.hadoop" % "hadoop-common" % "2.10.2" % "provided",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.10.2" % "provided")

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

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("shapeless.**" -> "shadeshapeless.@1")
    .inLibrary("com.netease.wm" % "wm-util_2.12" % "0.2.0")
    .inLibrary("com.chuusai" % "shapeless_2.12" % "2.3.2")
    .inProject,
  ShadeRule.rename("okhttp3.**" -> "shadeokhttp3.@1")
    .inLibrary("com.squareup.okhttp3" % "okhttp" % "3.10.0")
    .inLibrary("com.squareup.okhttp3" % "logging-interceptor" % "3.10.0")
    .inProject,
  ShadeRule.rename("com.fasterxml.jackson.databind.**" -> "shade.com.fasterxml.jackson.databind.@1")
    .inLibrary("com.fasterxml.jackson.core" % "jackson-databind" % "2.8.11")
    .inProject
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF", xs @ _* ) => MergeStrategy.discard
  case x => MergeStrategy.first
}