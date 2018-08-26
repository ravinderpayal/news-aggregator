name := "crawler"
 
version := "1.0" 
      
lazy val `Titanicturer` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

// https://mvnrepository.com/artifact/mysql/mysql-connector-java
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.11"

libraryDependencies += "io.lemonlabs" %% "scala-uri" % "1.1.4"

// libraryDependencies + = "akka.cl"

libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "4.11"



libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-play" % "0.16.0"
)

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "6.0.1"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "6.0.1"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3"
)
libraryDependencies += "org.jsoup" % "jsoup" % "1.11.3"
libraryDependencies += "com.github.ghostdogpr" % "readability4s" % "1.0.9"

libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.0"

enablePlugins(DockerPlugin)
enablePlugins(JavaAppPackaging)


dockerRepository := Some("asia.gcr.io/titanic-bcd7e")
//dockerUsername := Some("")
maintainer := "ravinderpayal"

// dockerExposedPorts in Docker := Seq(9000,80)
dockerUpdateLatest := true