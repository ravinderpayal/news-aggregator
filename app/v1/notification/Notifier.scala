package v1.notification

import javax.inject.{Inject, Singleton}
import models.AppUser
import utils.Email

import scala.concurrent.ExecutionContext


@Singleton
class AppNotifier @Inject()(notificationRepository: NotificationRepositoryImpl, email: Email)(implicit ec: ExecutionContext)  {
  def notify(user: AppUser, notification: NotificationData) {
    notificationRepository.create(notification)
    // Sends notification over socket
    //handleSocket.sendNotification(notification.userId, notification.toJsValue)

    // Send notification over email
    email.sendNotification(user, notification)
  }
}