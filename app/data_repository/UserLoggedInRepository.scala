package data_repository

import javax.inject.{Inject, Singleton}
import java.util.{Date, UUID}

import scala.language.implicitConversions
import scala.util.{Failure, Success}
import scala.concurrent.Future
import akka.actor.ActorSystem
import models.Gender.Gender
import models.{AppUser, AppUserProfile, UserLoggedIn}
import play.api.libs.concurrent.CustomExecutionContext
import play.api.libs.json._
import play.api.{Logger, MarkerContext}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.math.BigDecimal





class UserLoggedInExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "userLoggedInRepository.dispatcher")

/**
  * A pure non-blocking interface for the UserRepository.
  */
trait UserLoggedInRepository {
  def create(user: UserLoggedIn)(implicit mc: MarkerContext): Future[Option[Boolean]]
  // def setAttr[T](userId: UUID, attr: String, value:T)(implicit mc: MarkerContext): Future[Option[Boolean]]

  def get(id:String)(implicit mc: MarkerContext): Future[Option[AppUser]]

  // def verify(email:String, password:String)(implicit mc: MarkerContext): Future[Option[UserData]]
}

/**
  * A trivial implementation for the User Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class UserLoggedInRepositoryImpl @Inject()(implicit ec: UserLoggedInExecutionContext, val reactiveMongoApi: ReactiveMongoApi)
  extends UserLoggedInRepository{
  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("user_logged_in"))





  override def get(id: String)(implicit mc: MarkerContext): Future[Option[AppUser]] = {
    collection.flatMap (_.find(Json.obj("userId"->id)).one[AppUser])
  }

  def isExists(userId: UUID)(implicit mc: MarkerContext): Future[Option[AppUser]] = {
    val future = collection.flatMap (_.find(Json.obj("$or" -> List(Json.obj("email"-> userId), Json.obj("userId" -> userId)))).one[AppUser])
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
  def findByLoginInfo(provider: String, token: String)(implicit mc: MarkerContext): Future[Option[UserLoggedIn]] = {
    collection.flatMap(_.find(Json.obj("provider" -> provider, "token" -> token)).one[UserLoggedIn])
  }
  override def create(user: UserLoggedIn)(implicit mc: MarkerContext): Future[Option[Boolean]] = {
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
}