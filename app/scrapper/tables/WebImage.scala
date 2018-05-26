package scrapper.tables

import java.util.Date

import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class WebImage(tag: Tag) extends Table[(Int, String, String, String, Long, String, Boolean)](tag, "imagelinks") {
  def id = column[Int]("LINK_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def pageUrl = column[String]("PAGE_URL")
  def imgUrl = column[String]("IMG_URL")
  def imgALT = column[String]("IMG_ALT")
  def lastAccess = column[Long]("timestamp")
  def annotation = column[String]("annotation")
  def isAnnotated = column[Boolean]("isAnnotated")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, pageUrl, imgUrl, imgALT, lastAccess, annotation, isAnnotated)
}
object WebImage {
  val webimage = TableQuery[WebImage]
}


class Annotations(tag: Tag) extends Table[(Int,  String)](tag, "annotations") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def name = column[String]("annotation")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, name)
}
object Annotations {
  val annotations = TableQuery[Annotations]
}



// So here we defined the table strucute, we need not manually querry databse using SQl, everything is handled by the play-slick l;ibrary behinjd the scenes
// I hope this is not confusing