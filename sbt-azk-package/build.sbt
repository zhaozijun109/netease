name := "sbt-azk-package"
organization := "com.netease.wm"
version := "0.2.2"
scalaVersion := "2.12.10"
javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint")

sbtPlugin := true

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")

credentials += Credentials(Path.userHome / "sbt_comic_repo_credentials")
publishTo := Some("comic-releases" at "http://10.172.113.214:8081/content/repositories/releases")
