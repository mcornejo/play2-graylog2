name := "play2-graylog2"

organization := "org.graylog2"

version := "1.3.0-scala-SNAPSHOT"

scalaVersion := "2.11.5"

lazy val root = (project in file(".")).enablePlugins(PlayScala)


libraryDependencies ++= Seq(
  "org.graylog2" % "gelfclient" % "1.2.0",
  "com.google.guava" % "guava" % "18.0"
)

// Settings for publishing to Maven Central, see http://www.scala-sbt.org/0.13/docs/Using-Sonatype.html
publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

licenses := Seq("Apache 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/Graylog2/play2-graylog2"))

pomExtra := (
  <scm>
    <url>https://github.com/Graylog2/play2-graylog2</url>
    <connection>scm:git:https://github.com/Graylog2/play2-graylog2.git</connection>
    <developerConnection>scm:git:git@github.com:Graylog2/play2-graylog2.git</developerConnection>
  </scm>
    <developers>
      <developer>
        <id>kroepke</id>
        <name>Kay Roepke</name>
      </developer>
    </developers>)
