package utils

import javax.inject.Inject
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import resource.handlers.UserResourceHandler

import scala.language.implicitConversions

/**
 * Packages up the component dependencies for the User controller.
 *
 * This is a good way to minimize the surface area exposed to the controller, so the
 * controller only has to have one thing injected.
 */
case class UserControllerComponents @Inject()(userActionBuilder: AppActionBuilder,
                                              userActionBuilderAuthenticated: AppActionBuilderAuthenticated,
                                              userResourceHandler: UserResourceHandler,
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
class UserBaseController @Inject()(pcc: UserControllerComponents) extends BaseController with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = pcc

  def UserAction: AppActionBuilder = pcc.userActionBuilder
  def UserActionAuthenticated: AppActionBuilderAuthenticated = pcc.userActionBuilderAuthenticated

  def userResourceHandler: UserResourceHandler = pcc.userResourceHandler
}