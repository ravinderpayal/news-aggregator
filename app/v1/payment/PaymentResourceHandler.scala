package v1.payment

import java.util.{Date, UUID}

import javax.inject.{Inject, Provider}
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc.Results._
import play.api.mvc._
import utils.{ApplicationResult, Config}
import v1.askopinion.AskOpinionResourceHandler

// import v1.job.{JobData, JobRepositoryImpl}

import scala.concurrent.{ExecutionContext, Future}


/**
  * Controls access to and creation of the backend data
  */
class PaymentResourceHandler @Inject()(routerProvider: Provider[PaymentRouter],
                                       paymentRepository: PaymentRepositoryImpl,
                                       config: Config,
                                       ws: WSClient,
                                       askOpinionResourceHandler: AskOpinionResourceHandler
                                      )(implicit ec: ExecutionContext) {
  def get(userId:UUID, paymentId: UUID):Future[Result] = {
    paymentRepository.get(paymentId, userId).map(_ match {
      case Some(payment) => Ok(ApplicationResult("", true, "", Some(payment.toPublic.toJsValue)).toJsValue)
      case None => NotFound
    })
  }
  private var token: String = ""
  private val tokenGeneratedAt = 0L
  private def getAccessToken = {
    if ((new Date().getTime - tokenGeneratedAt) > 180000) // It will regenerate the token every 3 Minutes = 3 * 60 *1000 Milliseconds
      ws.url("https://api.sandbox.paypal.com/v1/oauth2/token").addHttpHeaders(
        "Accept" -> "application/json",
        "Accept-Language" -> "en_US").withAuth(
          "AZ1SRteh1HoXV3j45vY2zTvULOKQYukfh6VpQxJduuo6ulrYRKw3Xd-fRa2AbDJ6O53mt1FBtfMXSnY5",
          "EEWk91shZRV8BixBxO0R01SK05YTBK0bHsPjHr9xhzPgssNXS2OaRn8C-bSZg0zPhxQXDGcMTY_KU5c5",
           WSAuthScheme.BASIC).post(Map("grant_type" -> "client_credentials")).map{
        response =>
          token = (response.json \ "access_token").validate[String].getOrElse("")
          token
      }
    else Future.successful(token)
  }
  /*
  private def validatePaymentId(paymentId: UUID, userId: UUID)(f:Future[Result]):Future[Result] = {

  }
  */
  def createPaypal(paymentId: UUID, userId: UUID) = {
    paymentRepository.get(paymentId, userId).flatMap(_ match {
      case Some(payment) if !payment.status.success=> getAccessToken.flatMap { tokResponse =>
      val postData = Json.obj(
        "intent" -> "sale",
        "redirect_urls" -> Json.obj(
          "return_url" -> "https://mandisoft.com/app/payment/paypal/accepted",
          "cancel_url" -> "https://mandisoft.com/app/payment/paypal/cancelled"
        ),
        "payer" -> Json.obj("payment_method" -> "paypal"),
        "transactions" -> List(
          Json.obj(
            "amount"  -> Json.obj(
              "total" -> payment.money.amount,
              "currency" -> "USD",
              "details" -> Json.obj(
                "subtotal" -> payment.money.amount,
                "tax" -> "0")
              ),
            "item_list" -> Json.obj(
              "items" -> List(Json.obj(
                "quantity" -> "1",
                "name" -> "Course",
                "price" -> payment.money.amount,
                "currency" -> "USD",
                "description" -> payment.label,
                "tax" -> "0"))
              ),
            "description" -> payment.label,
            "invoice_number" -> payment.id.toString
          )
        )
      )
      // .toString()
      ws.url("https://api.sandbox.paypal.com/v1/payments/payment").addHttpHeaders(
        "Authorization" -> s"Bearer $tokResponse",
        "Content-Type" -> "application/json"
      ).post(
        postData.toString
      ).map {
        response => Ok(ApplicationResult("", true,  "", Some(response.json)).toJsValue)
      }
      }
      case None => Future.successful(NotAcceptable)
    })
  }
  def createPaytm(paymentId: UUID) = {
    paymentRepository.get(paymentId).flatMap(_ match {
      case Some(payment) if !payment.status.success=> getAccessToken.map { tokResponse =>
        val hash = utils.hash.sha256Hash(payment.toString)
        ws.url("https://api.sandbox.paypal.com/v1/payments/payment").addHttpHeaders(
          "Authorization" -> s"Bearer $tokResponse",
          "Content-Type" -> "application/json"
        ).post(
          "MID" -> config.paytmMid,
          "ORDER_ID" -> payment.successCallBack.referenceId,
          "CUST_ID" -> payment.userId,
          "TXN_AMOUNT" -> payment.money.amount,
          "CHANNEL_ID" -> "WEB",
          "INDUSTRY_TYPE_ID" -> config.paytmIndustryType,
          "WEBSITE" -> "WEB_STAGING",
          "CHECKSUMHASH" -> hash,
          "MOBILE_NO" -> payment.mobileNumber,
          "EMAIL" -> payment.email

        ).map {
          response => Ok(ApplicationResult("", true,  "", Some(response.json)).toJsValue)
        }

        Ok(v1.views.html.paytm(payment.userId, payment.money.amount, payment.successCallBack.referenceId, hash))
      }
      case None => Future.successful(NotAcceptable)
    })
  }

  def authorizedOnPaypal(paymentId: UUID, paypalPaymentId: String, paypalPayerId: String, userId: UUID) = {
    //    curl -v https://api.sandbox.paypal.com/v1/payments/payment/payment_id/execute/ \\
    //  -H "Content-Type:application/json" \\
    //  -H "Authorization: Bearer Access-Token" \\
    //  -d '{
    //  "payer_id": "payer_id"
    // }'
    paymentRepository.get(paymentId, userId).flatMap(_ match {
      case Some(payment) if !payment.status.success=> getAccessToken.flatMap { tokResponse =>
        getAccessToken.flatMap{tokResponse =>
          ws.url(s"https://api.sandbox.paypal.com/v1/payments/payment/${paypalPaymentId}/execute/").addHttpHeaders(
            "Authorization" -> s"Bearer $tokResponse",
            "Content-Type" -> "application/json"
          ).post(Json.obj("payer_id" -> paypalPayerId).toString).flatMap {
            response => {
              (response.json \ "state").asOpt[String] match {
                case Some(state) => {
                  paymentRepository.logPaypal(PaypalLog(paymentId, response.json))
                  if (state.equals("approved")) {
                    paymentRepository.updateStatus(paymentId, PaymentStatusPaid).map(_ match {
                      case Some(x) => {
                          Future{
                            onPaymentSuccess(payment)
                          }
                          Accepted(
                            ApplicationResult("", true, "", Some(Json.obj("status" -> PaymentStatusPaid))).toJsValue
                          )
                      }
                      case None => ServiceUnavailable(
                        ApplicationResult("", false, "", Some(Json.obj("error" -> "We failed to update your payment status. Sorry for inconvinience. Please retry."))).toJsValue
                      )
                    })
                  } else {
                    Future.successful(BadRequest(ApplicationResult("", false, "", Some(Json.obj("error" -> "Request actually not authorized by paypal"))).toJsValue))
                  }
                }
                case None=> Future.successful(ServiceUnavailable(ApplicationResult("", false, "", Some(Json.obj("error" -> response.json))).toJsValue))
              }
            }
          }
        }
      }
      case Some(payment) if payment.status.success => {
        // Add mechanism to log duplicate transactions
        Future.successful(Conflict)
      }
      case None => Future.successful(BadRequest)
    })
  }
  def processZero(paymentId: UUID, userId: UUID) = {
    paymentRepository.get(paymentId, userId).flatMap(_ match {
      case Some(payment) if !payment.status.success=> 
                  if (payment.money.amount == 0) {
                    paymentRepository.updateStatus(paymentId, PaymentStatusPaid).map(_ match {
                      case Some(x) => {
                          Future{
                            onPaymentSuccess(payment)
                          }
                          Accepted(
                            ApplicationResult("", true, "", Some(Json.obj("status" -> PaymentStatusPaid))).toJsValue
                          )
                      }
                      case None => ServiceUnavailable(
                        ApplicationResult("", false, "", Some(Json.obj("error" -> "We failed to update your payment status. Sorry for inconvinience. Please retry."))).toJsValue
                      )
                    })
                  } else {
                    Future.successful(BadRequest(ApplicationResult("", false, "", Some(Json.obj("error" -> "Malformed request!"))).toJsValue))
                  }
      case Some(payment) if payment.status.success => {
        // Add mechanism to log duplicate transactions
        Future.successful(Conflict)
      }
      case None => Future.successful(BadRequest)
    })
  }
  private def onPaymentSuccess(payment: PaymentData) = {
    payment.successCallBack.productClass match {
      case "ASK" => {
          askOpinionResourceHandler.onPaymentSuccess(payment)
      }
      case "WALLET" => {
        // TODO: Change code
      }
      case _ => {
        // TODO: Transfer amount to wallet
      }
    }
  }
  /**
    *
    * Only for use with Admin Panel. Don't jeopardize the system for using with concerns
    * @param paymentId
    * @return
    */
  def getForAdmin(paymentId: UUID):Future[Result] = {
    paymentRepository.getForAdmin(paymentId).map(_ match {
      case Some(payment) => Ok(ApplicationResult("", true, "", Some(payment.status.toJsValue)).toJsValue)
      case None => NotFound
    })
  }

}
