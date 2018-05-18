package scrapper

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import play.api.libs.json.{JsError, JsSuccess, Json}

class ApplicationEventBus(jobManager: JobManager, out: ActorRef) extends Actor {
  def receive = {
    case msg: String => {
          jobManager.scrap(msg)
    }
    case _ => None
  }

  override def postStop(): Unit = {
    super.postStop()
  }
}
object ApplicationEventBus {
  def props(jobManager: JobManager, out: ActorRef) = Props(new ApplicationEventBus(jobManager, out))
}

