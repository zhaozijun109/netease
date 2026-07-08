name := "common"

libraryDependencies ++= Seq(
  "com.squareup.okhttp3" % "okhttp" % "3.14.9",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.15.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.3",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.15.3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.3"
)

assembly / assemblyShadeRules := Seq(
  ShadeRule.rename("okhttp3.**" -> "shaded.okhttp3.@1").inAll,
  ShadeRule.rename("okio.**"    -> "shaded.okio.@1").inAll
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case x => MergeStrategy.first
}

credentials += Credentials(Path.userHome / "sbt_comic_repo_credentials")
publishTo := Some(("comic-releases" at "https://maven-lofter.hz.netease.com/content/repositories/releases").withAllowInsecureProtocol(true))