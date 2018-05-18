package v1.askopinion

import java.util.{Date, UUID}

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import v1.payment.PaymentStatusWithId
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json._
import play.api.{Logger, MarkerContext}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import v1.payment.PaymentStatusWithId

import scala.concurrent.Future
import scala.language.implicitConversions



class JobExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "userRepository.dispatcher")

/**
  * A pure non-blocking interface for the NotificationRepository.
  */
trait AskOpinionRepository {
  def create(data: AskOpinionData)(implicit mc: MarkerContext): Future[Option[Boolean]]

  def get(id:UUID, userId: UUID)(implicit mc: MarkerContext): Future[Option[AskOpinionData]]
  def get(userId: UUID, order: Int, pageSize: Int, pageNumber: Int)(implicit mc: MarkerContext): Future[List[AskOpinionData]]
  def get(userId: UUID, order: Int, skipN: Int)(implicit mc: MarkerContext): Future[List[AskOpinionData]]
  def updateFees(userId: UUID, id: UUID, status: PaymentStatusWithId)(implicit mc: MarkerContext): Future[Option[Boolean]]
}

/**
  * A trivial implementation for the Notification Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class AskOpinionRepositoryImpl @Inject()()(implicit ec: JobExecutionContext, val reactiveMongoApi: ReactiveMongoApi)
  extends AskOpinionRepository {

  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("v1/notification"))


  private val logger = Logger(this.getClass)

  override def get(id: UUID, userId: UUID)(implicit mc: MarkerContext): Future[Option[AskOpinionData]] = {
    collection.flatMap (_.find(Json.obj("notificationId"->id)).one[AskOpinionData])
  }
  override def get(userId: UUID, order: Int, pageSize: Int, pageNumber: Int)(implicit mc: MarkerContext): Future[List[AskOpinionData]] = {
    val skipN = (pageNumber - 1) * pageSize
    val qOps = new QueryOpts(skipN = skipN, batchSizeN = pageSize, flagsN = 0)
    collection.flatMap (_.find(
      Json.obj("userId" -> userId))
      .options(qOps)
      .sort(Json.obj("time" -> -1))
      .batchSize(pageSize)
      .cursor[AskOpinionData]()
      .collect[List](pageSize,Cursor.FailOnError[List[AskOpinionData]]()))
  }
  override def get(userId: UUID, order: Int, skipN: Int)(implicit mc: MarkerContext): Future[List[AskOpinionData]] = {
    val qOps = new QueryOpts(skipN = skipN, batchSizeN = 10, flagsN = 0)
    collection.flatMap (_.find(
      Json.obj("userId" -> userId))
      .options(qOps)
      .sort(Json.obj("time" -> -1))
      .batchSize(10)
      .cursor[AskOpinionData]()
      .collect[List](10,Cursor.FailOnError[List[AskOpinionData]]()))
  }


  override def create(data: AskOpinionData)(implicit mc: MarkerContext): Future[Option[Boolean]] = {
    collection.flatMap(_.insert(data)).map(_ => {
      Some(true)
    })
  }
  override def updateFees(userId: UUID, id: UUID, status: PaymentStatusWithId)(implicit mc: MarkerContext): Future[Option[Boolean]] = {
    collection.flatMap(_.update(Json.obj("userId" -> userId), Json.obj("$set" -> Json.obj("fees" -> status)), multi = true)).map( _ => {
      Some(true)
    })
  }

}


case class AskOpinionData(id: UUID,
                          title: String,
                          text: String,
                          userId: UUID,
                          time: Date,
                          answered: Boolean,
                          fees:PaymentStatusWithId,
                          opinion: Option[String]){
  def toJsValue = Json.toJson(this)
}
object AskOpinionData{
  implicit val format: OFormat[AskOpinionData] = Json.format[AskOpinionData]
}
