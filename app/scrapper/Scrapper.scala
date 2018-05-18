package scrapper

import javax.inject.Singleton
import org.jsoup.Jsoup

import scala.collection.JavaConversions._

@Singleton
class Scrapper{
  def scrap(link: String) = {
    val document = Jsoup.connect(link).get()
    val post_img = document.select(".content .post_img a.imagecache-imagelink")
    val post_img_description = document.select(".content .post_img .post_img_description")
    val article = document.select(".content .bv")
    val img_src = post_img.get(0).absUrl("href")
    val img_description = post_img_description.get(0).text
    document.select("img").map(a =>(a.absUrl("src"), a.attr("alt")))
  }
}
