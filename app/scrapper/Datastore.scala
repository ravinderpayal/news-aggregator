package scrapper

import java.util.Date

import javax.inject.Singleton
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scrapper.tables._
import slick.jdbc.meta.MTable

import scala.concurrent.duration.{Duration}
import scala.concurrent.{Await, Future}

@Singleton
class DataStore {
  val db = Database.forConfig("mysql")

  val tables = List(WebImage.webimage, WebImageAnnotation.webimageAnnotation, Annotations.annotations)
  val setup = DBIO.sequence(
    tables.map(_.schema.create.asTry)
  )

  Await.result(db.run(setup), Duration.Inf)

  def insert(pageLink: String, imgLink: String, imgAlt: String) = {
    db.run(
      DBIO.seq(
        WebImage.webimage.map(a => (a.pageUrl, a.imgUrl, a.imgALT, a.lastAccess, a.annotation, a.isAnnotated)) += ((pageLink, imgLink, imgAlt, new Date().getTime, "", false))
    )).map(a => println(a))
  }
  def addAnnotation(imgId: Int, annotation: String) = {
    db.run(
      DBIO.seq(
        WebImageAnnotation.webimageAnnotation forceInsertExpr (imgId, new Date().getTime, annotation),
        WebImage.webimage.filter(_.id === imgId).map(a => (a.annotation,a.isAnnotated,a.lastAccess)).update((annotation, true, new Date().getTime))
      ))
  }

  def removeImage(imgId: Int) = {
    db.run(WebImage.webimage.filter(_.id === imgId).delete)
  }

  def get(url: String) = {
    val q =     for {
      c <- WebImage.webimage if c.pageUrl === url && c.isAnnotated === false
    } yield (c.id, c.imgALT, c.imgUrl)

    db.run(q.result).map(a => a)
  }

  def countImageAnnotations = {
    db.run(WebImageAnnotation.webimageAnnotation.length.result)
  }

  def get = {
    val q = (for {
      c <- WebImage.webimage if c.isAnnotated === false
    } yield (c.pageUrl, c.lastAccess, c.isAnnotated)).groupBy(a => (a._1, a._3)).map{
      case(a,b) => (a._1, b.map(_._2).max, b.map(_._3).length, b.length)
    }
    db.run(q.result)
  }

}


