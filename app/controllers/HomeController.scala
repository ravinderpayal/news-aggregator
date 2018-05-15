package controllers

import java.util.Date

import javax.inject._
import models.Gender.Gender
import models.UserName
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

case class UserLoginForm(email:String, password: String)
case class UserSignupForm(name:UserName, email:String, password: String, gender: String, dob: Date)
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc)  with play.api.i18n.I18nSupport{
  val userLoginForm = Form(
    mapping(
      "email" -> email,
      "password" -> text(minLength = 8, maxLength = 20)
    )(UserLoginForm.apply)(UserLoginForm.unapply)
  )
  val userSignupForm = Form(
    mapping(
      "name" -> mapping(
        "firstName" -> text(minLength = 1, maxLength = 64),
        "middleName" -> optional(text(minLength = 1, maxLength = 64)),
        "lastName" -> text(minLength = 1, maxLength = 64)
      )(UserName.apply)(UserName.unapply),
      "email" -> email,
      "password" -> text(minLength = 8, maxLength = 20),
      "gender" -> text,
      "dob" -> date
    )(UserSignupForm.apply)(UserSignupForm.unapply)
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
  def login = Action {implicit request =>
    Ok(views.html.login(userLoginForm))
  }
  def signup = Action {implicit request =>
    Ok(views.html.signup(userSignupForm))
  }
  def signupPost = Action {implicit request =>
    val formValidationResult = userSignupForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
      BadRequest(views.html.signup(formWithErrors))
    }, { form =>
      Redirect(routes.HomeController.index())
    })
  }
  def loginPost = Action {implicit request =>
    val formValidationResult = userLoginForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
      BadRequest(views.html.login(formWithErrors))
    }, { form =>
      Redirect(routes.HomeController.index())
    })
  }

}
