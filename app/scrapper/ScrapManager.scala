package scrapper

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

case class ScrappedImages(pageLink: String, images: List[(String, String)])

@Singleton
class ScrapManager @Inject()(dataStore: DataStore, scrapper: Scrapper)(implicit executionContext: ExecutionContext) {

  def onScrapped(scrapped: ScrappedImages): Unit = {
    scrapped.images.foreach(a => dataStore.insert(scrapped.pageLink,a._1, a._2))
  }
  def get(link: String) = {
    dataStore.get(link).map(a => a)
  }
  // from here we will be getting links to be shown in table
  def get = {
    dataStore.get.map(a => a)
  }

  def annotate(imgId: Int, annotation: String) = {
        dataStore.addAnnotation(imgId, annotation)
  }

  def blankImage(imgId: Int) = dataStore.removeImage(imgId)

  def countImageAnnotations = dataStore.countImageAnnotations

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
  def props(scrapManager: ScrapManager, superVisor: CrawlerSupervisor) = Props(new ScrapManagerActor(scrapManager, superVisor))
}

class ScrapManagerActor(scrapManager: ScrapManager, superVisor: CrawlerSupervisor) extends Actor {
  def receive = (a: Any) => a match {
    case b:ScrappedImages =>
      scrapManager.onScrapped(b)
    case _ => superVisor.supervisorActor ! a // TODO: wrap it into unsupported
  }

  override def postStop(): Unit = {
    super.postStop()
  }
}


case class NewUrl(url:String)