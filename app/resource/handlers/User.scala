package resource.handlers

import java.util.{Date, UUID}

import data_repository.UserRepositoryImpl
import javax.inject.Inject
import models.Gender.Gender
import models._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import utils.{Config, jwt}

import scala.concurrent.{ExecutionContext, Future}

class UserResourceHandler @Inject()(userRepository: UserRepositoryImpl,
                                   config: Config)(implicit ec: ExecutionContext) {

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
