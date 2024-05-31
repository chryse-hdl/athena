ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/chryse-hdl/athena"),
    "scm:git@github.com/chryse-hdl/athena.git",
  ),
)

ThisBuild / developers := List(
  Developer(
    id = "kivikakk",
    name = "Asherah Connor",
    email = "ashe@kivikakk.ee",
    url = url("https://kivikakk.ee"),
  ),
)

ThisBuild / description := "Chisel libraries, developed for use with Chryse."
ThisBuild / licenses := List(
  "Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"),
)

usePgpKeyHex("4ADC6C1E368DB976CCAF886B0D22B80CB8F0D344")

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishMavenStyle    := true

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots".at(nexus + "content/repositories/snapshots"))
  else Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
}
