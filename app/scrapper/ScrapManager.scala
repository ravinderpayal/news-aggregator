package scrapper

import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

case class ScrappedArticle(pageLink: String, image:(String, String), title: String, article: String)

@Singleton
class ScrapManager @Inject()(dataStore: DataStore, scrapper: Scrapper)(implicit executionContext: ExecutionContext) {

  def onScrapped(scrapped: ScrappedArticle): Unit = {
    // println("Scrapped is called")
    dataStore.upsert(scrapped.pageLink,scrapped.image._1, scrapped.image._2, scrapped.title, scrapped.article)
  }
  def shouldICrawl(url: String): Future[Boolean] = {
    dataStore.get(url, new Date().getTime - 43200000).map(_.nonEmpty)
  }

  def get(id: Int) = {
    dataStore.get(id).map(a => a)
  }

  def top4 = {
    dataStore.get().map(a => a)
  }

  // from here we will be getting links to be shown in table
  def get(skipN: Int, pageSize: Int) = {
    dataStore.get(skipN, pageSize).map(a => a)
  }


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
  def receive = (a: Any) => a match {
    case b:ScrappedArticle =>
      scrapManager.onScrapped(b)
    case NewUrl(url) =>
      scrapManager.shouldICrawl(url).foreach(if (_) superVisor.scrapperActor ! NewUrl(url) else println("already crawled: " + url))
    case _ => superVisor.supervisorActor ! a // TODO: wrap it into unsupported
  }

  override def postStop(): Unit = {
    super.postStop()
  }
}


case class NewUrl(url:String)