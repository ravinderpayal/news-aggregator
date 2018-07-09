package scrapper.tables

import java.util.Date

import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class Article(tag: Tag) extends Table[(Int, String, String, String, String, String, Long)](tag, "article") {
  def id = column[Int]("LINK_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def sourceUrl = column[String]("SOURCE_URL", O.Unique, O.Length(1024, true))
  def sourceDominName = column[String]("SOURCE_DOMAIN", O.Unique, O.Length(1024, true))
  def imgUrl = column[String]("IMG_URL")
  def imgALT = column[String]("IMG_ALT")
  def title = column[String]("TITLE")
  def article = column[String]("ARTICLE")
  def createdAt = column[Long]("TIMESTAMP")
  def addedAt = column[Long]("TIMESTAMP")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, pageUrl, imgUrl, imgALT, title, article, createdAt)
}

object Article {
  val article = TableQuery[Article]
}

