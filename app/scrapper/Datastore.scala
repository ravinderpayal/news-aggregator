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
    WebImage.webimage.schema.create
  )
  db.run(setup)

  def insert(pageLink: String, imgLink: String, imgAlt: String) = {
    db.run(
    DBIO.seq(
      WebImage.webimage forceInsertExpr (WebImage.webimage.length + 1, pageLink, imgLink, imgAlt, new Date().getTime)
    ))
  }
  def get(url: String) = {
    val q =     for {
      c <- WebImage.webimage if c.pageUrl === url
    } yield (c.imgALT, c.imgUrl)

    db.run(q.distinctOn(_._1).groupBy(_._2).result).map(a => a)
  }
  def get = {
    val q =     for {
      c <- WebImage.webimage
    } yield (c.pageUrl, c.lastAccess)

    db.run(q.distinctOn(_._1).result).map(a => a)
  }

}


