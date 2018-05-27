package v1.file

import java.util.Date

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json._
import play.api.{Logger, MarkerContext}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.{Failure, Success}

sealed trait UserGender {
  val description: String
  val intCode: Int
}
object UserGender{
  implicit def intValue(a: UserGender):Int={
    a.intCode
  }
}
final object GenderMale extends UserGender{
  override val description: String = "Male"
  override val intCode: Int = 1
}
final object GenderFemale extends UserGender{
  override val description: String = "Female"
  override val intCode: Int = 1

}
final object GenderOther extends UserGender{
  override val description: String = "Other"
  override val intCode: Int = 1
}


class UserExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "userRepository.dispatcher")

/**
  * A pure non-blocking interface for the PostRepository.
  */
trait FileRepository {
  def create(data: FileData)(implicit mc: MarkerContext): Future[Option[Boolean]]

  // def list()(implicit mc: MarkerContext): Future[Iterable[UserData]]

  // def get(id: UserId)(implicit mc: MarkerContext): Future[Option[UserData]]

  def get(id:String)(implicit mc: MarkerContext): Future[Option[FileData]]
}

/**
  * A trivial implementation for the User Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class FileRepositoryImpl @Inject()()(implicit ec: UserExecutionContext, val reactiveMongoApi: ReactiveMongoApi)
  extends FileRepository{

  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("files"))

  def collectionBS: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("files"))

  private val logger = Logger(this.getClass)

  override def get(id: String)(implicit mc: MarkerContext): Future[Option[FileData]] = {
    collection.flatMap (_.find(Json.obj("id"->id)).one[FileData])
  }


  override def create(data: FileData)(implicit mc: MarkerContext): Future[Option[Boolean]] = {

    val writeRes: Future[WriteResult] = collection.flatMap(_.insert(data))

    writeRes.onComplete {
      case Failure(e) => {
        // todo: add log handler
      }
      case Success(writeResult) => None
    }

    writeRes.map(_ => {
      Some(true)
    })
  }
}

/**
  *
  * @param name Actual Name of File
  * @param contentType Content Type of the file
  * @param size In bytes
  */
case class FileMeta(name:String, contentType:String, size: Long)
object FileMeta{
  implicit val userFullNameFormat: OFormat[FileMeta] = Json.format[FileMeta]
}

case class FileData(meta: FileMeta, size: Long, uploadTime: Date, id: String, category: Int, tempPath:String)
object FileData{
  implicit val userRegistrationDbInsertFormat: OFormat[FileData] = Json.format[FileData]
}

object FileCategories{
  val PROFILE_PICTURE = 1
}