package payment

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
    paymentResourceHandler.get(UUID.fromString(request.userId), id)
  }
  def processZero(paymentId: UUID): Action[AnyContent] = PaymentActionAuthenticated.async{
    implicit request =>
      paymentResourceHandler.processZero(paymentId, UUID.fromString(request.userId))
  }
  def createPaypal(paymentId: UUID): Action[AnyContent] = PaymentActionAuthenticated.async{
    implicit request =>
      paymentResourceHandler.createPaypal(paymentId, UUID.fromString(request.userId))
  }
  def authorizedOnPaypal(paymentId: UUID, paypalPaymentId: String, payerId: String): Action[AnyContent] = PaymentActionAuthenticated.async{
    implicit request =>
      paymentResourceHandler.authorizedOnPaypal(paymentId, paypalPaymentId, payerId, UUID.fromString(request.userId))
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
