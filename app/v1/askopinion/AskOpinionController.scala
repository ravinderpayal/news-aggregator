package v1.askopinion

import java.util.UUID

import javax.inject.{Inject, Singleton}
import models.UserName
import play.api.data.{Form, Forms}
import play.api.data.Forms.{email, mapping, optional, text}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class AskQuestionForm(subject: String, question: String)
case class AskQuestionFormWithoutUser(name:UserName, email:String, mobileNumber: Long, subject: String, question: String) {
  def toQuestionForm = AskQuestionForm(subject, question)
}

/**
  *  Name:- AskOpinionController
  *  Jobs:- Get Latest, Get Specific, Mark Seen
  *
*/
@Singleton
class AskOpinionController @Inject()(cc: AskOpinionControllerComponents)(implicit ec: ExecutionContext)
  extends AskOpinionBaseController(cc) with play.api.i18n.I18nSupport {

  def get(id: UUID): Action[AnyContent] = AskOpinionActionAuthenticated.async { implicit request =>
    askOpinionResourceHandler.get(request.user.id, id)
  }

  def getLatest(counter: Int): Action[AnyContent] = AskOpinionActionAuthenticated.async { implicit request =>
    askOpinionResourceHandler.getLatest(request.user.id, counter)
  }

  val askQuestionForm = Form(
    mapping(
      "subject" -> text(minLength = 16, maxLength = 128),
      "question" -> text(minLength = 32, maxLength = 4096)
    )(AskQuestionForm.apply)(AskQuestionForm.unapply)
  )

  val askQuestionFormWithoutUser = Form(
    mapping(
      "name" -> mapping(
        "firstName" -> text(minLength = 2, maxLength = 128),
        "middleName" -> optional(text(minLength = 2, maxLength = 128)),
        "lastName" -> text(minLength = 2, maxLength = 128)
      )(UserName.apply)(UserName.unapply),
      "email" -> email,
      "mobileNumber" -> Forms.longNumber(min = 1000000000L, max = 9999999999L),
      "subject" -> text(minLength = 16, maxLength = 128),
      "question" -> text(minLength = 32, maxLength = 4096)
    )(AskQuestionFormWithoutUser.apply)(AskQuestionFormWithoutUser.unapply)
  )

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = AskOpinionAction{ implicit request =>
    request.user match {
      case Some(user) => Ok(v1.views.html.askquestion(askQuestionForm)(request,request2Messages))
      case None => Ok(v1.views.html.askquestionwithoutuser(askQuestionFormWithoutUser)(request,request2Messages))

    }
  }

  def ask = AskOpinionAction.async { implicit request =>
    request.user match {
      case Some(user) =>         askQuestionForm.bindFromRequest()(request).fold({ formWithErrors =>
        Future.successful(BadRequest(v1.views.html.askquestion(formWithErrors)(request, request2Messages)))
      }, { form =>
        askOpinionResourceHandler.create(user,form)
      })
      case None =>
        askQuestionFormWithoutUser.bindFromRequest()(request).fold({ formWithErrors =>
          Future.successful(BadRequest(v1.views.html.askquestionwithoutuser(formWithErrors)(request, request2Messages)))
        }, { form =>
          askOpinionResourceHandler.create(form)
        })
    }
  }
}
