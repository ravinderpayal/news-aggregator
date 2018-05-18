package resource.handlers

import java.util.{Date, UUID}

import controllers.{UserLoginForm, UserSignupForm}
import data_repository.UserRepositoryImpl
import javax.inject.Inject
import models.Gender.Gender
import models._
import pdi.jwt.JwtSession.RichResult
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import utils.{Config, jwt}

import scala.concurrent.{ExecutionContext, Future}

class UserResourceHandler @Inject()(userRepository: UserRepositoryImpl,
                                   config: Config)(implicit ec: ExecutionContext) {
  def create(form: UserSignupForm):Future[Result] = {
    userRepository.create(AppUser(UUID.randomUUID, None, form.email, form.mobileNumber.toString, Some(utils.hash.sha256Hash(form.password)), Gender(form.gender), AppUserProfile(form.name, None, Some(form.dob)))) map(_ match {
      case Some(x) if x => Ok
      case None => ServiceUnavailable
    })
  }
  import pdi.jwt.JwtSession._
  def login(form: UserLoginForm):Future[Result] = {
    userRepository.verify(email = form.email, passWord = utils.hash.sha256Hash(form.password)) map(_ match {
      case Some(x) => Redirect(v1.enterurl.routes.EnterUrlController.index()).withJwtSession(genLoginSession(x.toUserLoggedIn(LoginProvider.Email)))
      case None => ServiceUnavailable
    })
  }

  private def genLoginSession(userLoggedIn: UserLoggedIn) = {
    jwt.generate(config.apiHost, userLoggedIn)
  }

  def updateProfile(userId: UUID, titanicUserProfile: AppUserProfile):Future[Result] = {
    userRepository.updateProfile(userId, titanicUserProfile) map{
      case Some(x) => if (x) {
        Results.Ok(titanicUserProfile.toString)
      } else {
        Results.ServiceUnavailable
      }
      case None => Results.ServiceUnavailable
    }
  }

}
