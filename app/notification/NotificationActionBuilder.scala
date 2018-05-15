package notification

import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import utils.{AppActionBuilder, AppActionBuilderAuthenticated, RequestMarkerContext}

import scala.language.implicitConversions

/**
 * Packages up the component dependencies for the User controller.
 *
 * This is a good way to minimize the surface area exposed to the controller, so the
 * controller only has to have one thing injected.
 */
case class NotificationControllerComponents @Inject()(appActionBuilder: AppActionBuilder,
                                                      appActionBuilderAuthenticated: AppActionBuilderAuthenticated,
                                                      notificationResourceHandler: NotificationResourceHandler,
                                                      actionBuilder: DefaultActionBuilder,
                                                      parsers: PlayBodyParsers,
                                                      messagesApi: MessagesApi,
                                                      langs: Langs,
                                                      fileMimeTypes: FileMimeTypes,
                                                      executionContext: scala.concurrent.ExecutionContext)
  extends ControllerComponents

/**
 * Exposes actions and handler to the PostController by wiring the injected state into the base class.
 */
class NotificationBaseController @Inject()(pcc: NotificationControllerComponents) extends BaseController with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = pcc

  def NotificationAction: AppActionBuilder = pcc.appActionBuilder
  def NotificationActionAuthenticated: AppActionBuilderAuthenticated = pcc.appActionBuilderAuthenticated

  def notificationResourceHandler: NotificationResourceHandler = pcc.notificationResourceHandler
}
