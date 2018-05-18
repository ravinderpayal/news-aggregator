package scrapper

import java.util.Date

import javax.inject.Singleton
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scrapper.tables._

@Singleton
class DataStore {
  val db = Database.forConfig("h2mem1")
  val setup = DBIO.seq(
    // Create the tables, including primary and foreign keys
    WebImage.webimage.schema.create
  )

  def insert(pageLink: String, imgLink: String, imgAlt: String) = DBIO.seq(
    WebImage.webimage forceInsertExpr (WebImage.webimage.length + 1, pageLink, imgLink, imgAlt, new Date().getTime)
  )
}


