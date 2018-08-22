package scrapper

import java.util.Date

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.RoundRobinPool
import com.github.ghostdogpr.readability4s.Readability
import javax.inject.{Inject, Singleton}
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import io.lemonlabs.uri.Url
import play.api.libs.ws.WSClient

@Singleton
class Scrapper @Inject()(ws:WSClient)(implicit ec: ExecutionContext) {
  var counter = 0
  def scrap(domain: String, link: String):Future[Option[(Option[ScrappedArticle], List[String])]] = {
    counter+=1
    ws.url(link).get().map(result => {
      if (result.status == 200 && result.contentType.contains("text/html")) {
        try {

          val document = Jsoup.parse(result.body)
          counter -= 1
          val all_links = document.select("a").asScala.map(_.absUrl("href"))
          println(link + " : " + all_links.length.toString + " sub-links found")
          val parsed = Readability(link, result.body).parse()
          parsed match {
            case Some(article) =>
              Some(value = (Some(ScrappedArticle(domainName = domain, pageLink = link, favicon = article.faviconUrl, image = (article.imageUrl, ""), title = article.title, excerpt = article.excerpt, article = article.content, createdAt = new Date().getTime)), all_links.toList))
            case None =>
              Some(value = (None, all_links.toList))
          }

          // val post_img = document.select(".content .post_img a.imagecache-imagelink")
          // val post_img_description = document.select(".content .post_img .post_img_description")


          //  val title = document.select("h2 zag_page").text
          //  val article = document.select(".content .bv")
          /// val text = article.text
          //  val img_src = if (post_img.size() > 0) post_img.get(0).absUrl("href") else ""
          //  val img_description = if (post_img_description.size() > 0) post_img_description.get(0).text else ""

        } catch {
          case e: Exception =>
            // println("/*************************Handled Exception**********/\n\n")
            // e.printStackTrace()
            // println("\n\n/*************************Handled Exception**********/")
            counter -= 1
            None
          case _: Throwable =>
            counter -= 1
            None
        }
      } else {
        println(result.contentType)
        None
      }
      })
  }
}

object ScrapperActor {
  def props(scrapper: Scrapper, crawlerSupervisor: CrawlerSupervisor)(implicit ec:ExecutionContext) = Props(new ScrapperActor(scrapper, crawlerSupervisor))
}

class ScrapperActor (scrapper: Scrapper, crawlerSupervisor: CrawlerSupervisor) (implicit ec: ExecutionContext) extends Actor {
  def receive = {
    case (domain:String, NewUrl(url)) =>
      scrapper.scrap(domain, url) map {
        case Some(scrapped) =>
            onScrapped(scrapped, url)
        case None => println("Bad luck...found nothing for given URL: " + url)
      }
  }

  def onScrapped(scrapped:(Option[ScrappedArticle], List[String]), url: String) = {
    // if (url.contains ("/en/content/") )
    if (scrapped._1.isDefined)
      crawlerSupervisor.scrapManagerActor ! scrapped._1.get
    scrapped._2.foreach (url2 => {
      // println(url2)
      crawlerSupervisor.scrapManagerActor ! NewUrl (url2)
    })
  }

  override def postStop(): Unit = {
    // todo: what to do? Just Relax
  }
}
