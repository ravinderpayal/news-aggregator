package v1.askopinion

import java.util.{Date, UUID}

import data_repository.UserRepositoryImpl
import javax.inject.{Inject, Provider}
import models._
import v1.payment.{PaymentData, PaymentOnSuccessCallBack, PaymentStatusInitiated}
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import utils.{ApplicationResult, Config, Money}
import v1.payment.{PaymentData, PaymentOnSuccessCallBack, PaymentRepositoryImpl, PaymentStatusInitiated}

import scala.concurrent.{ExecutionContext, Future}


/**
  * Controls access to and creation of the backend data
  */
class EnterUrlResourceHandler @Inject()(routerProvider: Provider[EnterUrlRouter],
                                        userRepository: UserRepositoryImpl,
                                        askOpinionRepository: AskOpinionRepositoryImpl,
                                        paymentRepository: PaymentRepositoryImpl,
                                        config: Config)(implicit ec: ExecutionContext) {
  def create(userId: UUID, questionForm: AskQuestionForm) = {
  }
  def create(user: UserLoggedIn, questionForm: AskQuestionForm) = {
    val questionId = UUID.randomUUID()
    val time = new Date()
    val payment = PaymentData(UUID.randomUUID(),
        user.id, user.mobileNumber, user.email, Money(config.perOpinionFees, "INR"),
        time, PaymentStatusInitiated,
        s"Payment for opinion: ${questionId.toString}",
        PaymentOnSuccessCallBack("CLASS_ROOM", questionId), None, None)
    val question = AskOpinionData(questionId, questionForm.subject, questionForm.question, user.id, time, false, payment.toStatusWithId, None)
    askOpinionRepository.create(question).flatMap(_ match {
      case Some(x) if x =>     paymentRepository.create(payment).map (_ match {
        case Some(r) => {
          if (r) {
            // Ok(ApplicationResult("", true, "", Some(payment.toPublic.toJsValue)).toJsValue)
            Redirect(v1.payment.routes.PaymentController.createPaytm(payment.id.toString))
          } else InternalServerError
        }
        case None => InternalServerError
      })
      case _ => Future.successful(ServiceUnavailable)
    })
  }
  def create(questionForm: AskQuestionFormWithoutUser):Future[Result] = {
    val user = AppUser(UUID.randomUUID, None, questionForm.email, questionForm.mobileNumber.toString, None, Gender.Others, AppUserProfile(questionForm.name, None, None))
    userRepository.create(user) flatMap (_ match {
      case Some(x) if x => {
        create(user.toUserLoggedIn(LoginProvider.Email), questionForm.toQuestionForm)
      }
      case _ => Future.successful(ServiceUnavailable)
    })
  }

  def get(userId: UUID, id:UUID) = {
    println(id)
    askOpinionRepository.get(userId, id).map{
      a => a match {
        case Some(obj) => {
          Ok(ApplicationResult("", true, "", Some(obj.toJsValue)).toJsValue)
        }
        case None => NotFound
      }
    }
  }

  def getLatest(userId: UUID, counter: Int = 0) = {
    askOpinionRepository.get(userId, -1, counter).map{
      notifications => {
        Ok(
          ApplicationResult(
            "Latest Notifications",
            true,
            "OK",
            Some(Json.obj("notifications" -> notifications))).toJsValue)
      }
    }
  }

  def onPaymentSuccess(payment: PaymentData) = {
    askOpinionRepository.updateFees(payment.userId, payment.successCallBack.referenceId, payment.toStatusWithId)
  }
}
