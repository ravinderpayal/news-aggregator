package data_repository

import javax.inject.{Inject, Singleton}
import java.util.{Date, UUID}

import scala.language.implicitConversions
import scala.util.{Failure, Success}
import scala.concurrent.Future
import akka.actor.ActorSystem
import models.Gender.Gender
import models.{AppUser, AppUserProfile}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json._
import play.api.{Logger, MarkerContext}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.math.BigDecimal





class UserExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "userRepository.dispatcher")

/**
  * A pure non-blocking interface for the UserRepository.
  */
trait UserRepository {
  def create(user: AppUser)(implicit mc: MarkerContext): Future[Option[Boolean]]
  def get(id: UUID)(implicit mc: MarkerContext): Future[Option[AppUser]]
}

/**
  * A trivial implementation for the User Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class UserRepositoryImpl @Inject()()(implicit ec: UserExecutionContext, val reactiveMongoApi: ReactiveMongoApi)
  extends UserRepository{
  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("users"))





  override def get(id: UUID)(implicit mc: MarkerContext): Future[Option[AppUser]] = {
    collection.flatMap (_.find(Json.obj("userId"->id)).one[AppUser])
  }

  def verify(email: String, passWord:String)(implicit mc: MarkerContext): Future[Option[AppUser]] = {
    collection.flatMap (_.find(Json.obj("email"->email, "password"->passWord)).one[AppUser])
  }
  def isExists(email:String, facebookId: String)(implicit mc: MarkerContext): Future[Option[AppUser]] = {
    val future = collection.flatMap (_.find(Json.obj("$or" -> List(Json.obj("email"-> email), Json.obj("facebookId" -> facebookId)))).one[AppUser])
    future onComplete {
      case Success(u) => {
      }
      case Failure(f) => {
        println(f)
        None
      }
    }
    future
  }

  override def create(user: AppUser)(implicit mc: MarkerContext): Future[Option[Boolean]] = {
    val writeRes: Future[WriteResult] = collection.flatMap(_.insert(user))

    writeRes.onComplete {
      case Failure(e) => {
        println(e.getMessage)

        // todo: add log manager
      }
      case Success(_) => None
    }

    writeRes.map(_ => {
      Some(true)
    })
  }


  def updateProfilePicture(fileId: String, userId:String): Future[Option[Boolean]] ={
    val selector = Json.obj("userId" -> userId)

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "profilePicture" -> fileId))
    collection.flatMap(_.update(selector, modifier)).map( _ => {
      Some(true)
    })
  }
  def updateProfile(userId:UUID, profile: AppUserProfile): Future[Option[Boolean]] ={
    val selector = Json.obj("userId" -> userId)
    val modifier = Json.obj(
      "$set" -> Json.obj(
        "profile" -> profile)
    )
    collection.flatMap(_.update(selector, modifier)).map( _ => {
      Some(true)
    })
  }
}