package v1.payment

import java.util.UUID

import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


/**
  *  Name:- PaymentController
  *  Jobs:- Make Payment, Get Status
  *
*/
class PaymentController @Inject()(cc: PaymentControllerComponents)(implicit ec: ExecutionContext)
  extends PaymentBaseController(cc) {
  def get(id:UUID): Action[AnyContent] = PaymentActionAuthenticated.async{implicit request =>
    paymentResourceHandler.get(request.user.id, id)
  }
  def processZero(paymentId: UUID): Action[AnyContent] = PaymentActionAuthenticated.async{
    implicit request =>
      paymentResourceHandler.processZero(paymentId, request.user.id)
  }
  def createPaypal(paymentId: UUID): Action[AnyContent] = PaymentActionAuthenticated.async{
    implicit request =>
      paymentResourceHandler.createPaypal(paymentId, request.user.id)
  }
  def createPaytm(paymentId: String): Action[AnyContent] = PaymentAction.async{
    implicit request =>
      paymentResourceHandler.createPaytm(UUID.fromString(paymentId))
  }

  def authorizedOnPaypal(paymentId: UUID, paypalPaymentId: String, payerId: String): Action[AnyContent] = PaymentActionAuthenticated.async{
    implicit request =>
      paymentResourceHandler.authorizedOnPaypal(paymentId, paypalPaymentId, payerId, request.user.id)
  }
  // TODO: Complete make payment
  def make: Action[AnyContent] = PaymentActionAuthenticated.async{
    implicit request => Future.successful(Ok("Will be implemented"))
  }

  // TODO: Listen payment gateway
  def listen: Action[AnyContent] = PaymentAction.async{
    implicit request =>
      println(request)
      Future.successful(Ok("Will be implemented"))
  }
}
