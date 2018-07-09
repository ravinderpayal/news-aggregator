package scrapper

import java.util.Date

import javax.inject.Singleton
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scrapper.tables._

import scala.concurrent.duration.{Duration}
import scala.concurrent.{Await, Future}

@Singleton
class DataStore {

  val db = Database.forConfig("mysql")


  val tables = List(Article.article)
  val setup = DBIO.sequence(
    tables.map(_.schema.create.asTry)
  )

  Await.result(db.run(setup).map(println(_)), Duration.Inf)

  def insert(pageLink: String, imgLink: String, imgAlt: String, title: String, article: String) = {
    db.run(DBIO.seq(Article.article.map(a => (a.pageUrl, a.imgUrl, a.imgALT, a.title, a.article, a.createdAt)) += ((pageLink, imgLink, imgAlt, title, article, new Date().getTime)))).map(a => println(a))
  }

  def upsert(pageLink: String, imgLink: String, imgAlt: String, title: String, article: String) = {
    db.run(Article.article.insertOrUpdate((0, pageLink, imgLink, imgAlt, title, article, new Date().getTime)))
  }

  def removeArticle(imgId: Int) = {
    db.run(Article.article.filter(_.id === imgId).delete)
  }

  def get(id: Int) = {
    val q =     for {
      c <- Article.article if c.id === id
    } yield (c.id, c.imgALT, c.imgUrl, c.title, c.article)

    db.run(q.result).map(a => a)
  }

  def get(link: String, lastCrawl: Long) = {
    db.run(Article.article.filter(f => f.pageUrl === link && f.createdAt > lastCrawl).map(f => f.pageUrl).result)
  }

  def countArticles = {
    db.run(Article.article.length.result)
  }

  def get(skipN: Int, pageSize: Int) = {
    val q = for {
      c <- Article.article
    } yield (c.id, c.imgALT, c.imgUrl, c.title, c.article)
    db.run(q.result)
  }
}


