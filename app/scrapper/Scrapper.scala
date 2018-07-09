package scrapper

import akka.actor.{Actor, ActorRef, Props}
import javax.inject.{Inject, Singleton}
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import io.lemonlabs.uri.Url

@Singleton
class Scrapper {
  def scrap(link: String):Option[(ScrappedArticle, mutable.Buffer[String])] = {
    try {
      val document = Jsoup.connect(link).get()
      //val post_img = document.select(".content .post_img a.imagecache-imagelink")
      //val post_img_description = document.select(".content .post_img .post_img_description")
      //val article = document.select(".content .bv")
      //val img_src = post_img.get(0).absUrl("href")
      //val img_description = post_img_description.get(0).text
      // map is basically a function which iterates/loop over each element of list and apply some method,]
      // then collect the result of each applied function and return as list, here returning src attribute of each img tag and alt attribute in the
      // the format: (src.alt)
      val post_img = document.select(".content .post_img a.imagecache-imagelink")
      val post_img_description = document.select(".content .post_img .post_img_description")


      val title = document.select("h2 zag_page").text
      val article = document.select(".content .bv")
      val text = article.text
      val img_src = if (post_img.size() > 0) post_img.get(0).absUrl("href") else ""
      val img_description = if (post_img_description.size() > 0) post_img_description.get(0).text else ""
      val all_links = document.select("a").asScala.map(_.absUrl("href"))

      Some((ScrappedArticle(link, (img_src, img_description), title, text), all_links))
    } catch {
      case e: Exception =>
        println("/*************************Handled Exception**********/\n\n")
        e.printStackTrace()
        println("\n\n/*************************Handled Exception**********/")
        None
      case _: Throwable => None
    }
  }
}

object ScrapperActor {
  def props(scrapper: Scrapper, crawlerSupervisor: CrawlerSupervisor) = Props(new ScrapperActor(scrapper, crawlerSupervisor))
}

class ScrapperActor(scrapper: Scrapper, crawlerSupervisor: CrawlerSupervisor) extends Actor {
  def receive = {
    case NewUrl(url) =>
      // println("scraping new URL")
      scrapper.scrap(url) match {
        case Some(scrapped) =>
          val parsed = Url.parse(url)
          val path = parsed.path
          parsed.hostOption match {
            case Some(host) =>
              host.apexDomain match {
                case Some(domain) => {
                  println(domain)
                  onScrapped(scrapped, url)
                }
                case None =>
              }
            case None =>
          }
        case None => None
      }
  }

  def onScrapped(scrapped:(String, List[String]), url: String) = {
    if (url.contains ("/en/content/") )
      crawlerSupervisor.scrapManagerActor ! scrapped._1
    scrapped._2.foreach (url2 => {
      crawlerSupervisor.scrapperActor ! NewUrl (url2)
    })
  }

  override def postStop(): Unit = {
    // todo: what to do? Just Relax
  }
}
