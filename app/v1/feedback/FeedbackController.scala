/**
  * @author Ravinder Payal <mail@ravinderpayal.com>
  * @since v1 2017-11-07
  */
package v1.feedback

import java.util.UUID

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.mvc.{Action, AnyContent, Result}
import utils.AuthenticatedAppRequest

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  * @param about Tells what's feedback all-about
  * @param text Text of feedback.
  */
case class FeedbackInput(about: String, text: String)


/**
  *  Name:- FeedbackController
  *  Jobs:- Save and Retrieve
  *
*/
class FeedbackController @Inject()(cc: FeedbackControllerComponents)(implicit ec: ExecutionContext)
    extends FeedbackBaseController(cc) {
  val feedbackForm: Form[FeedbackInput] = Form(mapping(
      "about" -> text,
      "text" -> text
    )(FeedbackInput.apply)(FeedbackInput.unapply))

  def get(feedbackId: UUID):Action[AnyContent] = FeedbackActionAuthenticated.async{ implicit request =>
    // TODO: to be implemented
    Future.successful(Ok("To be implemented"))
  }

  /**
    *
    * @param userId UUID Id of the user whom current session is rating
    * @return
    */
  def save():Action[AnyContent] = FeedbackActionAuthenticated.async{ implicit request =>
    processSaveReq()
  }
  def processSaveReq[A]()(implicit request: AuthenticatedAppRequest[A]): Future[Result] = {
    def failure(badForm: Form[FeedbackInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }
    def success(input: FeedbackInput) = {
      feedbackResourceHandler.save(request.user.id, input)
    }
    feedbackForm.bindFromRequest().fold(failure, success)
  }
}
