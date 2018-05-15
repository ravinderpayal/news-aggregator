package notification

import java.util.UUID

import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext


/**
  *  Name:- NotificationController
  *  Jobs:- Get Latest, Get Specific, Mark Seen
  *
*/
class NotificationController @Inject()(cc: NotificationControllerComponents)(implicit ec: ExecutionContext)
  extends NotificationBaseController(cc) {
  def get(id:UUID): Action[AnyContent] = NotificationActionAuthenticated.async{implicit request =>
    notificationResourceHandler.get(request.user.id, id)
  }
  def getLatest(counter: Int): Action[AnyContent] = NotificationActionAuthenticated.async{implicit request =>
    notificationResourceHandler.getLatest(request.user.id, counter)
  }

  /**
    * Type: Endpoint
    * Purpose: Mark all notifications till now seen/read
    * Authentication: Required
    */
  def markSeen: Action[AnyContent] = NotificationActionAuthenticated.async{
    implicit request => notificationResourceHandler.markSeen(request.user.id)
  }
}
