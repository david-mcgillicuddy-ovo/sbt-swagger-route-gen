organization in ThisBuild := "com.ovoenergy.sbt-swagger-gen-routes"
name := "sbt-swagger-gen-routes"

sbtPlugin := true
version := "0.1.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq (
  "io.swagger.core.v3" % "swagger-core" % "2.0.4",
  "io.swagger.parser.v3" % "swagger-parser" % "2.0.4",
  "org.scalameta" %% "scalameta" % "4.0.0",
  scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided
)

organizationName := "OVO Energy"
startYear := Some(2018)
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
