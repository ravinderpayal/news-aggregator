package scrapper.tables

import java.util.Date

import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class Article(tag: Tag) extends Table[(Int, String, String, String, String, String, String, String, String, Long, Long)](tag, "article") {
  def id = column[Int]("LINK_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def sourceUrl = column[String]("SOURCE_URL", O.Unique, O.Length(512, true))
  def sourceLogo = column[String]("SOURCE_LOGO", O.Length(1024, true))
  def sourceDomainName = column[String]("SOURCE_DOMAIN", O.Length(140, true))
  def imgUrl = column[String]("IMG_URL")
  def imgALT = column[String]("IMG_ALT")
  def title = column[String]("TITLE")
  def excerpt = column[String]("EXCERPT")
  def article = column[String]("ARTICLE")
  def createdAt = column[Long]("CREATED_AT")
  def addedAt = column[Long]("TIMESTAMP")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, sourceDomainName, sourceUrl, sourceLogo, imgUrl, imgALT, title, excerpt, article, createdAt, addedAt)
}

object Article {
  val article = TableQuery[Article]
}

