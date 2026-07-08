name := "sbt-db-dump"
organization := "com.netease.wm"
version := "2.8.2"
scalaVersion := "2.12.10"
javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint")

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "mysql" % "mysql-connector-java" % "5.1.35"
)

credentials += Credentials(Path.userHome / "sbt_comic_repo_credentials")
publishTo := Some("comic-releases" at "https://maven-lofter.hz.netease.com/content/repositories/releases")
