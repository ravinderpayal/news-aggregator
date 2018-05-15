package feedback

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
case class FeedbackControllerComponents @Inject()(appActionBuilder: AppActionBuilder,
                                                  appActionBuilderAuthenticated: AppActionBuilderAuthenticated,
                                                  feedbackResourceHandler: FeedbackResourceHandler,
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
class FeedbackBaseController @Inject()(pcc: FeedbackControllerComponents) extends BaseController with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = pcc

  def FeedbackAction: AppActionBuilder = pcc.appActionBuilder
  def FeedbackActionAuthenticated: AppActionBuilderAuthenticated = pcc.appActionBuilderAuthenticated

  def feedbackResourceHandler: FeedbackResourceHandler = pcc.feedbackResourceHandler
}
