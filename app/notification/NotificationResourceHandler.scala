package notification

import java.util.UUID

import javax.inject.{Inject, Provider}
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import utils.ApplicationResult

import scala.concurrent.{ExecutionContext, Future}


/**
  * Controls access to and creation of the backend data
  */
class NotificationResourceHandler @Inject()(
                                             routerProvider: Provider[NotificationRouter],
                                             notificationRepository: NotificationRepositoryImpl,
                                             config: Configuration)(implicit ec: ExecutionContext) {

  def get(userId: UUID, id:UUID) = {
    println(id)
    notificationRepository.get(userId, id).map{
      a => a match {
        case Some(obj) => {
          Ok(ApplicationResult("", true, "", Some(obj.toJsValue)).toJsValue)
        }
        case None => NotFound
      }
    }
  }

  def getLatest(userId: UUID, counter: Int = 0) = {
    notificationRepository.get(userId, -1, counter).map{
      notifications => {
        Ok(
          ApplicationResult(
            "Latest Notifications",
            true,
            "OK",
            Some(Json.obj("notifications" -> notifications))).toJsValue)
      }
    }
  }

  /**
    * Type: Request processor
    * Purpose: Request processor for endpoint 'notification/markSeen'
    * @return Future[Result]
    */
  def markSeen(userId: UUID):Future[Result] = {
    notificationRepository.markSeen(userId).map(_ match {
      case Some(x) => {
        if(x) {
          Ok(ApplicationResult("Mark notification seen", true, "", None).toJsValue)
        } else {
          InternalServerError
        }
      }
    case None => InternalServerError
    })
  }

}
