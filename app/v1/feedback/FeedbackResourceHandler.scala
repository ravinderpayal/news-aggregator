package v1.feedback

import java.util.{Date, UUID}

import data_repository.UserRepositoryImpl
import javax.inject.{Inject, Provider}
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Configuration, MarkerContext, _}
import utils.ApplicationResult

import scala.concurrent.{ExecutionContext, Future}


/**
  * Controls access to and creation of the backend data
  */
class FeedbackResourceHandler @Inject()(
                                         routerProvider: Provider[FeedbackRouter],
                                         feedbackRepository: FeedbackRepositoryImpl,
                                         userRepository: UserRepositoryImpl,
                                         config: Configuration)(implicit ec: ExecutionContext) {
  private val logger = Logger(this.getClass)


  def save(userId: UUID, rating: FeedbackInput)(implicit mc: MarkerContext): Future[Result] = {
    feedbackRepository.save(FeedbackData(
      UUID.randomUUID(),
      userId, rating.about,
      rating.text,
      new Date())).map{
        case Some(obj) => if (obj) {
          Created(Json.toJson(ApplicationResult(
            "",
            true,
            "",
            None)))
            } else InternalServerError
        case None => InternalServerError
      }
  }

  def get(userId: UUID, id:UUID) = {
    feedbackRepository.getByUser(userId, id).map{
        case Some(feedbackData) => {
          ApplicationResult("", true, "", Some(feedbackData)).toJsValue
        }
        case None => NotFound
      }
  }
}