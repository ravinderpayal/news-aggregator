package feedback

import java.util.{Date, UUID}

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json._
import play.api.{Logger, MarkerContext}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.Future
import scala.language.implicitConversions


class FeedbackExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "feedbackRepository.dispatcher")

/**
  * A pure non-blocking interface for the RateRepository.
  */
trait FeedbackRepository {
  def save(data: FeedbackData)(implicit mc: MarkerContext): Future[Option[Boolean]]

  def get(feedbackId:UUID)(implicit mc: MarkerContext): Future[Option[FeedbackData]]
  def getByUser(userId: UUID, feedbackId: UUID)(implicit mc: MarkerContext): Future[Option[FeedbackData]]
  def getByUser(userId: UUID, pageSize: Int = 10, pageNumber: Int = 1)(implicit mc: MarkerContext): Future[List[FeedbackData]]
}

/**
  * A trivial implementation for the Feedback Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class FeedbackRepositoryImpl @Inject()()(implicit ec: FeedbackExecutionContext, val reactiveMongoApi: ReactiveMongoApi)
  extends FeedbackRepository{

  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("feedback"))

  private val logger = Logger(this.getClass)

  override def getByUser(userId: UUID, feedbackId:UUID)(implicit mc: MarkerContext): Future[Option[FeedbackData]] = {
    collection.flatMap (_.find(Json.obj("uderId" -> userId, "feedbackId" -> feedbackId)).one[FeedbackData])
  }
  override def getByUser(userId: UUID, pageSize: Int = 10, pageNumber: Int = 1)(implicit mc: MarkerContext): Future[List[FeedbackData]] = {
    val findObj = Json.obj("userId" -> userId)
    val skipN = (pageNumber - 1) * pageSize
    val qOps = new QueryOpts(skipN = skipN, batchSizeN = pageSize, flagsN = 0)
    collection.flatMap (_.find(findObj).options(qOps)
      .sort(Json.obj("time" -> -1)).batchSize(pageSize)
      .cursor[FeedbackData]().collect[List](pageSize,Cursor.FailOnError[List[FeedbackData]]()))
  }
  override def get(feedbackId: UUID)(implicit mc: MarkerContext): Future[Option[FeedbackData]] = {
    collection.flatMap (_.find(Json.obj("feedbackId"-> feedbackId)).one[FeedbackData])
  }

  override def save(data: FeedbackData)(implicit mc: MarkerContext): Future[Option[Boolean]] = {
    collection.flatMap(_.insert(data)).map{a=>
      Some(true)
    }
  }
}

// TODO: Add enumeration for feedback 
case class FeedbackData(feedbackId: UUID, userId: UUID, about: String, text: String, time: Date){
  def toJsValue = Json.toJson(this)
}
object FeedbackData{
  implicit val feedbackDataFormat: OFormat[FeedbackData] = Json.format[FeedbackData]
  implicit def toJsValue(fd: FeedbackData): JsValue = fd.toJsValue
}
