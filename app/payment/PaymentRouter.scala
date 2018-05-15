package payment

import java.util.UUID

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import utils.extractors
/**
  * Routes and URLs to the Payment controller.
  */
class PaymentRouter @Inject()(controller: PaymentController) extends SimpleRouter {
  val prefix = "/api/v1/payment"

  override def routes: Routes = {
   /* case GET(p"/") =>
      controller.index
    case GET(p"/get/latest") =>
      controller.getLatest()*/
    case GET(p"/$id") =>
      controller.get(UUID.fromString(id))
    case GET(p"/invoice/$paymentId/paypal/create") =>
      controller.createPaypal(UUID.fromString(paymentId))
    case POST(p"/invoice/${extractors.uuid(paymentId)}/zero") =>
      controller.processZero(paymentId)
    case POST(p"/${extractors.uuid(paymentId)}/invoice/paypal/$paypalPaymentId/$payerId/authorized") =>
      controller.authorizedOnPaypal(paymentId, paypalPaymentId, payerId)
  }

}
