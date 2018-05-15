package payment

import java.util.{Date, UUID}

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json._
import play.api.{Logger, MarkerContext}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.{Failure, Success}



class PaymentExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "userRepository.dispatcher")

/**
  * A pure non-blocking interface for the PaymentRepository.
  */
trait PaymentRepository {
  def create(data: PaymentData)(implicit mc: MarkerContext): Future[Option[Boolean]]

  /**
    * For general payment requests of users.
    * @param paymentId
    * @param userId
    * @param mc
    * @return
    */
  def get(paymentId:UUID, userId: UUID)(implicit mc: MarkerContext): Future[Option[PaymentData]]

  /**
    *
    * Only for use with Admin Panel. Don't jeopardize the system for using with concerns
    * @param paymentId
    * @param mc
    * @return
    */
  def getForAdmin(paymentId: UUID)(implicit mc: MarkerContext): Future[Option[PaymentData]]
  def getLatest(userId: UUID)(implicit mc: MarkerContext): Future[List[PaymentData]]

  def updateStatus(paymentId: UUID, status: PaymentStatus)(implicit mc: MarkerContext): Future[Option[Boolean]]

  def logPaypal(log: PaypalLog)(implicit mc: MarkerContext): Future[Option[Boolean]]
}

/**
  * A trivial implementation for the Payment Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class PaymentRepositoryImpl @Inject()()(implicit ec: PaymentExecutionContext, val reactiveMongoApi: ReactiveMongoApi)
  extends PaymentRepository {

  def paymentCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("payments"))
  def paypalCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("paymentsPaypal"))

  private val logger = Logger(this.getClass)

  override def get(paymentId: UUID, userId: UUID)(implicit mc: MarkerContext): Future[Option[PaymentData]] = {
    paymentCollection.flatMap (_.find(Json.obj("paymentId" -> paymentId, "userId" -> userId)).one[PaymentData])
  }

  override def getForAdmin(paymentId: UUID)(implicit mc: MarkerContext): Future[Option[PaymentData]] = {
    paymentCollection.flatMap (_.find(Json.obj("paymentId"->paymentId)).one[PaymentData])
  }
  override def getLatest(userId: UUID)(implicit mc: MarkerContext): Future[List[PaymentData]] = {
    paymentCollection.flatMap (_.find(
      Json.obj("seen" -> false, "userId" -> userId)).cursor[PaymentData]().collect[List](10,Cursor.FailOnError[List[PaymentData]]()))
  }


  override def create(data: PaymentData)(implicit mc: MarkerContext): Future[Option[Boolean]] = {

    val writeRes: Future[WriteResult] = paymentCollection.flatMap(_.insert(data))

    writeRes.onComplete {
      case Failure(e) => {
        e.printStackTrace()
        println(e)
      }
      case Success(_) => None
    }

    writeRes.map(_ => {
      Some(true)
    })
  }
  override def updateStatus(paymentId: UUID, status: PaymentStatus)(implicit mc: MarkerContext): Future[Option[Boolean]] = {
    paymentCollection.flatMap(
      _.update(Json.obj("paymentId" -> paymentId), Json.obj("$set" -> Json.obj("status" -> status)))
    ).map(_ => {
        Some(true)
      })
  }
  override def logPaypal(log: PaypalLog)(implicit mc: MarkerContext): Future[Option[Boolean]] = {
    paypalCollection.flatMap(_.insert(log)).map(_ => {
      Some(true)
    })
  }
}
case class PaypalLog(paymentId: UUID, log: JsValue)
object PaypalLog{
  implicit val paypalLogFormat: OFormat[PaypalLog] = Json.format[PaypalLog]
}
case class PaymentStatus(state: Int, message: String, success: Boolean){
  def toJsValue: JsValue = Json.toJson(this)
}
object PaymentStatus{
  implicit val paymentStatusFormat: OFormat[PaymentStatus] = Json.format[PaymentStatus]
}
object PaymentStatusInitiated extends PaymentStatus(1, "Payment process initiated waiting for further action", false) {
 /* override val state = 1
  override val message = "Payment initiated waiting for further action"
  override val success = false
*/
}

object PaymentStatusUserCheckedOut extends PaymentStatus(101, "User cheked out", false) {
}
object PaymentStatusPaid extends PaymentStatus(200, "Paid", true){
//  def apply: PaymentStatus = PaymentStatus(200, "Paid", true)
}

object PaymentStatusUserCancelled extends PaymentStatus(400, "Payment cancelled by user", false) {
}

object PaymentStatusMarkedCancelled extends PaymentStatus(400, "Payment marked cancelled by payment service", false) {
}

object PaymentStatusTransferPending extends PaymentStatus(120, "Waiting for the funds to arrive", false) {
}

object PaymentStatusTransferTakingTooLong extends PaymentStatus(150, "Fund transfer taking too long", false) {
}




case class PaymentData(paymentId: UUID,
                       userId: UUID,
                       money:Money,
                       time: Date,
                       status: PaymentStatus,
                       label: String,
                       successCallBack: PaymentOnSuccessCallBack,
                       data: Option[JsValue],
                       dataFromGateway: Option[JsValue]){
  def toJsValue = Json.toJson(this)
  def toPublic = PaymentDataPublic(paymentId, userId, money, time, status, label)
}

case class  PaymentOnSuccessCallBack(productClass: String, productId: UUID)
object PaymentOnSuccessCallBack {
  implicit val paymentOnSuccessCallBack: OFormat[PaymentOnSuccessCallBack] = Json.format[PaymentOnSuccessCallBack]
}

object PaymentData{
  implicit val paymentDataFormat: OFormat[PaymentData] = Json.format[PaymentData]
}

case class PaymentDataPublic(paymentId: UUID, userId: UUID, money:Money, time: Date, status: PaymentStatus, label: String){
  def toJsValue = Json.toJson(this)
}
object PaymentDataPublic{
  implicit val paymentDataPublicFormat: OFormat[PaymentDataPublic] = Json.format[PaymentDataPublic]
}
