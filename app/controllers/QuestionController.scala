package controllers

import javax.inject._
import play.api.data.{Form}
import play.api.data.Forms._
import play.api.mvc._

case class AskQuestionForm(name:String, email:String, question: String)
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class QuestionController @Inject()(cc: ControllerComponents) extends AbstractController(cc)  with play.api.i18n.I18nSupport{
  val askQuestionForm = Form(
    mapping(
      "name" -> text(minLength = 2, maxLength = 128),
      "email" -> email,
      "question" -> text(minLength = 128, maxLength = 4096)
    )(AskQuestionForm.apply)(AskQuestionForm.unapply)
  )
  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def ask = Action {implicit request =>
    Ok(views.html.askquestion(askQuestionForm))
    val formValidationResult = askQuestionForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
      BadRequest(views.html.askquestion(formWithErrors))
    }, { form =>
      Redirect(routes.HomeController.index())
    })
  }

}
