package scrapper.tables

import java.util.Date

import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class WebImageAnnotation(tag: Tag) extends Table[(Int, Long, String)](tag, "imageannotations") {
  def imgId = column[Int]("imgId") // This is the primary key column
  def timestamp = column[Long]("timestamp")
  def annotation = column[String]("annotation")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (imgId, timestamp, annotation)
}
object WebImageAnnotation {
  val webimageAnnotation = TableQuery[WebImageAnnotation]
}
