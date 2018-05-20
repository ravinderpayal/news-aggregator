name := "Titanicturer"
 
version := "1.0" 
      
lazy val `titanicturer` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

libraryDependencies += "com.h2database" % "h2" %"1.4.196"

// libraryDependencies + = "akka.cl"

libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "4.11"

val reactiveMongoVer = "0.12.6-play26"
libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer
)
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
