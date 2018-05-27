package scrapper

import akka.actor.{Actor, ActorRef, Props}
import javax.inject.{Inject, Singleton}
import org.jsoup.Jsoup

import scala.collection.JavaConverters._

@Singleton
class Scrapper {
  def scrap(link: String) = {
    val document = Jsoup.connect(link).get()
    //val post_img = document.select(".content .post_img a.imagecache-imagelink")
    //val post_img_description = document.select(".content .post_img .post_img_description")
    //val article = document.select(".content .bv")
    //val img_src = post_img.get(0).absUrl("href")
    //val img_description = post_img_description.get(0).text
    // map is basically a function which iterates/loop over each element of list and apply some method,]
    // then collect the result of each applied function and return as list, here returning src attribute of each img tag and alt attribute in the
    // the format: (src.alt)
     ScrappedImages(link, document.select("img").asScala.toList.map(a =>(a.absUrl("src"), a.attr("alt"))))
  }
}

object ScrapperActor {
  def props(scrapper: Scrapper, crawlerSupervisor: CrawlerSupervisor) = Props(new ScrapperActor(scrapper, crawlerSupervisor))
}

class ScrapperActor(scrapper: Scrapper, crawlerSupervisor: CrawlerSupervisor) extends Actor {
  def receive = {
    case NewUrl(url) =>
      scrapper.scrap(url)
  }

  override def postStop(): Unit = {
    // todo: what to do? JUst Relax
  }
}
