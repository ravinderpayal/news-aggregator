package scrapper

import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import io.lemonlabs.uri.DomainName
import javax.inject.{Inject, Singleton}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ScrappedArticle(domainName: String, pageLink: String, favicon: String, image:(String, String), title: String, excerpt: String, article: String, createdAt: Long)

@Singleton
class ScrapManager @Inject()(dataStore: DataStore, scrapper: Scrapper)(implicit executionContext: ExecutionContext) {

  def onScrapped(scrapped: ScrappedArticle): Unit = {
    // println("Scrapped is called")
    dataStore.upsert(domainName = scrapped.domainName, sourceUrl = scrapped.pageLink, sourceLogo = scrapped.favicon, imgLink = scrapped.image._1, imgAlt = scrapped.image._2, title = scrapped.title, excerpt = scrapped.excerpt, article = scrapped.article, createdAt = scrapped.createdAt) // .map(f=> println(f))
  }
  def shouldICrawl(url: String): Future[Boolean] = {
    dataStore.get(url, new Date().getTime - 43200000).map(_==0)
  }

  def get(id: Int) = {
    dataStore.get(id).map(a => a)
  }
  /*
  def top4 = {
    dataStore.get().map(a => a)
  }*/

  // from here we will be getting links to be shown in table
  def get(skipN: Int, pageSize: Int):Future[Seq[(Int, String, String, String, String, String, String)]] = {
    dataStore.get(skipN, pageSize)
  }

  def getCounter = scrapper.counter


  def countArticles = dataStore.countArticles

    // Here we are returning a Future wrapper and not defining the logic, as I am busy, we will do this later
}


@Singleton
class CrawlerSupervisor @Inject()(scrapManager: ScrapManager,scrapper: Scrapper)(implicit executionContext: ExecutionContext) {
  val system = ActorSystem()
  val supervisorActor = system.actorOf(CrawlerSupervisorActor.props( this))
  val scrapManagerActor = system.actorOf(ScrapManagerActor.props(scrapManager, this))
  val scrapperActor = system.actorOf(ScrapperActor.props(scrapper, this))
}

class CrawlerSupervisorActor(crawlerSupervisor: CrawlerSupervisor) extends Actor {
  def receive = {
    case a: Any =>
      println(a, "is not a supported act")
  }

  override def postStop(): Unit = {
    // todo: what to do? JUst Relax
  }
}

object CrawlerSupervisorActor {
  def props(crawlerSupervisor: CrawlerSupervisor) = Props(new CrawlerSupervisorActor(crawlerSupervisor))
}


object ScrapManagerActor {
  def props(scrapManager: ScrapManager, superVisor: CrawlerSupervisor)(implicit ec: ExecutionContext) = Props(new ScrapManagerActor(scrapManager, superVisor))
}

class ScrapManagerActor(scrapManager: ScrapManager, superVisor: CrawlerSupervisor)(implicit executionContext: ExecutionContext) extends Actor {
  val localMutableArray = mutable.MutableList()
  def receive = (a: Any) => a match {
    case b:ScrappedArticle =>
      scrapManager.onScrapped(b)
    case NewUrl(url) =>
      //if (!localMutableArray.contains(url)) {
      //  localMutableArray.+=(url)
        scrapManager.shouldICrawl(url).onComplete {
          case Success(s) if s => superVisor.scrapperActor ! NewUrl(url)
          case Failure(a) =>
            println(a)
            println(url + "problem checking this URL")
            //superVisor.scrapManagerActor ! NewUrl(url)
          case x =>
            println(x)
            println(url + " can't bne crawled")
        }
      //}
    case _ => superVisor.supervisorActor ! a // TODO: wrap it into unsupported
  }

  override def postStop(): Unit = {
    super.postStop()
  }
}


case class NewUrl(url:String)