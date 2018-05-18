package controllers

import java.util.Date

import com.google.common.util.concurrent.Futures.FutureCombiner
import javax.inject._
import models.Gender.Gender
import models.UserName
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.Future

case class UserLoginForm(email:String, password: String)
case class UserSignupForm(name:UserName, email:String, mobileNumber: Long, password: String, gender: String, dob: Date)
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: UserControllerComponents) extends UserBaseController(cc)  with play.api.i18n.I18nSupport{
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
      "mobileNumber" -> longNumber(min = 1000000000, max = 9999999999L),
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
  def about = Action {
    Ok(views.html.aboutus())
  }

  def login = Action {implicit request =>
    Ok(views.html.login(userLoginForm))
  }
  def signup = Action {implicit request =>
    Ok(views.html.signup(userSignupForm))
  }
  def signupPost = UserAction.async{implicit request =>
    request.user match {
      case None =>
        val formValidationResult = userSignupForm.bindFromRequest
        formValidationResult.fold({ formWithErrors =>
          Future.successful(BadRequest(views.html.signup(formWithErrors)(request, request2Messages)))
        }, { form =>
          userResourceHandler.create(form)
        })
      case Some(user) => Future.successful(Redirect(routes.HomeController.index()))
    }
  }

  def loginPost = Action.async{implicit request =>
    val formValidationResult = userLoginForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
     Future.successful(BadRequest(views.html.login(formWithErrors)(request, request2Messages)))
    }, { form =>
      userResourceHandler.login(form)
    })
  }

  /*

  Sandbox Merchant IDLANDPE06640339647382
Website Urlhttp://www.takeouropinion.com
Sandbox Merchant KeydoCAtqhHKZWN9@fC
Channel IdWEB
Industry TypeRetail
   */

}
