package v1.notification

import java.util.{Date, UUID}

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json._
import play.api.{Logger, MarkerContext}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.Future
import scala.language.implicitConversions



class JobExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "userRepository.dispatcher")

/**
  * A pure non-blocking interface for the NotificationRepository.
  */
trait NotificationRepository {
  def create(data: NotificationData)(implicit mc: MarkerContext): Future[Option[Boolean]]
  def markSeen(userId: UUID)(implicit mc: MarkerContext): Future[Option[Boolean]]

  def get(id:UUID, userId: UUID)(implicit mc: MarkerContext): Future[Option[NotificationData]]
  def get(userId: UUID, order: Int, pageSize: Int, pageNumber: Int)(implicit mc: MarkerContext): Future[List[NotificationData]]
  def get(userId: UUID, order: Int, skipN: Int)(implicit mc: MarkerContext): Future[List[NotificationData]]
}

/**
  * A trivial implementation for the Notification Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class NotificationRepositoryImpl @Inject()()(implicit ec: JobExecutionContext, val reactiveMongoApi: ReactiveMongoApi)
  extends NotificationRepository {

  def notificationCollection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("v1/notification"))

  def notificationCollectionBS: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("v1/notification"))

  private val logger = Logger(this.getClass)

  override def get(id: UUID, userId: UUID)(implicit mc: MarkerContext): Future[Option[NotificationData]] = {
    notificationCollection.flatMap (_.find(Json.obj("notificationId"->id)).one[NotificationData])
  }
  override def get(userId: UUID, order: Int, pageSize: Int, pageNumber: Int)(implicit mc: MarkerContext): Future[List[NotificationData]] = {
    val skipN = (pageNumber - 1) * pageSize
    val qOps = new QueryOpts(skipN = skipN, batchSizeN = pageSize, flagsN = 0)
    notificationCollection.flatMap (_.find(
      Json.obj("userId" -> userId))
      .options(qOps)
      .sort(Json.obj("time" -> -1))
      .batchSize(pageSize)
      .cursor[NotificationData]()
      .collect[List](pageSize,Cursor.FailOnError[List[NotificationData]]()))
  }
  override def get(userId: UUID, order: Int, skipN: Int)(implicit mc: MarkerContext): Future[List[NotificationData]] = {
    val qOps = new QueryOpts(skipN = skipN, batchSizeN = 10, flagsN = 0)
    notificationCollection.flatMap (_.find(
      Json.obj("userId" -> userId))
      .options(qOps)
      .sort(Json.obj("time" -> -1))
      .batchSize(10)
      .cursor[NotificationData]()
      .collect[List](10,Cursor.FailOnError[List[NotificationData]]()))
  }


  override def create(data: NotificationData)(implicit mc: MarkerContext): Future[Option[Boolean]] = {
    notificationCollection.flatMap(_.insert(data)).map(_ => {
      Some(true)
    })
  }
  override def markSeen(userId: UUID)(implicit mc: MarkerContext): Future[Option[Boolean]] = {
    notificationCollection.flatMap(_.update(Json.obj("userId" -> userId), Json.obj("$set" -> Json.obj("seen" -> true)), multi = true)).map( _ => {
      Some(true)
    })
  }

}


case class NotificationData(notificationId: UUID, title: String,
                            text: String, userId: UUID,
                            time: Date, link: String,
                            icon: Option[NotificationIcon],
                            data: Option[JsValue], seen: Boolean, clicked: Boolean){
  def toJsValue = Json.toJson(this)
}
object NotificationData{
  implicit val notificationDataFormat: OFormat[NotificationData] = Json.format[NotificationData]
}

/*sealed trait NotificationImage{
  val iType: Int
}*/
case class NotificationIcon(iType: Int, fileId: UUID)
object  NotificationIcon {
  implicit val notificationIconFormat: OFormat[NotificationIcon] = Json.format[NotificationIcon]
}
/*case class NotificationImageFile( fileId: String) extends NotificationIcon {
  val iTtype =  2
}*/
