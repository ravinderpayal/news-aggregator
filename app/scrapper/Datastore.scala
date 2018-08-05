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
  var counter = 0
  val db = Database.forConfig("mysql")


  val tables = List(Article.article)
  val setup = DBIO.sequence(
    tables.map(_.schema.create.asTry)
  )

  Await.result(db.run(setup).map(a => {
    println("/****\n\n\n\n\n\n\n\n\n\n\n\n\n")
    println(a)
    println("\n\n\n\n\n\n\n\n\n\n\n\n\n*************/")
  }), Duration.Inf)

  def insert(domainName: String, sourceUrl: String, sourceLogo: String, imgLink: String, imgAlt: String, title: String, excerpt: String, article: String, createdAt: Long) = {
    db.run(DBIO.seq(Article.article.map(a => (a.sourceDomainName, a.sourceUrl, a.sourceLogo, a.imgUrl, a.imgALT, a.title, a.excerpt, a.article, a.createdAt, a.addedAt)) += ((domainName, sourceUrl, sourceLogo, imgLink, imgAlt, title, excerpt, article, createdAt, new Date().getTime)))).map(a => println(a))
  }

  def upsert(domainName: String, sourceUrl: String, sourceLogo: String, imgLink: String, imgAlt: String, title: String, excerpt: String, article: String, createdAt: Long) = {
    counter += 1
    db.run(Article.article.insertOrUpdate(value = (0, domainName, sourceUrl, sourceLogo, imgLink, imgAlt, title, excerpt, article, createdAt, new Date().getTime)))
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
    db.run(Article.article.filter(f => f.sourceUrl === link && f.createdAt > lastCrawl).map(f => f.sourceUrl).result)
  }

  def countArticles = {
    db.run(Article.article.length.result)
  }

  def get(skipN: Int, pageSize: Int) = {
    val q = for {
      c <- Article.article
    } yield (c.id, c.imgALT, c.imgUrl, c.title, c.excerpt)
    db.run(q.result)
  }
}


