package scrapper.tables

import java.util.Date

import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class WebImage(tag: Tag) extends Table[(Int, String, String, String, Long)](tag, "links") {
  def id = column[Int]("LINK_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def pageUrl = column[String]("PAGE_URL")
  def imgUrl = column[String]("IMG_URL")
  def imgALT = column[String]("IMG_ALT")
  def lastAccess = column[Long]("timestamp")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, pageUrl, imgUrl, imgALT, lastAccess)
}
object WebImage {
  val webimage = TableQuery[WebImage]
}