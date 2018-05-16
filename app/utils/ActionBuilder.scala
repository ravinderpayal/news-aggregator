package utils

// Enables implicit conversions
import java.util.UUID

import scala.language.implicitConversions
import javax.inject.Inject
import models.{LoginProvider, UserLoggedIn}
import net.logstash.logback.marker.LogstashMarker
import play.api.{Logger, MarkerContext}
import play.api.http.HttpVerbs
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.Results.Forbidden
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import pdi.jwt.JwtSession._


/**
  * A wrapped request for post resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
trait AppRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
class AppRequest[A](val user: Option[UserLoggedIn], request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with AppRequestHeader
class AuthenticatedAppRequest[A](val user:UserLoggedIn, request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with AppRequestHeader

/**
 * Provides an implicit marker that will show the request in all logger statements.
 */
trait RequestMarkerContext {
  import net.logstash.logback.marker.Markers

  private def marker(tuple: (String, Any)) = Markers.append(tuple._1, tuple._2)

  private implicit class RichLogstashMarker(marker1: LogstashMarker) {
    def &&(marker2: LogstashMarker): LogstashMarker = marker1.and(marker2)
  }

  implicit def requestHeaderToMarkerContext(implicit request: RequestHeader): MarkerContext = {
    MarkerContext {
      marker("id" -> request.id) && marker("host" -> request.host) && marker("remoteAddress" -> request.remoteAddress)
    }
  }

}

/**
  * The action builder for the User resource.
  *
  * This is the place to put logging, metrics, to augment
  * the request with contextual data, and manipulate the
  * result.
  */
class AppActionBuilder @Inject()(messagesApi: MessagesApi, playBodyParsers: PlayBodyParsers)
                                 (implicit val executionContext: ExecutionContext)
    extends ActionBuilder[AppRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type UserRequestBlock[A] = AppRequest[A] => Future[Result]


  override def invokeBlock[A](request: Request[A],
                              block: UserRequestBlock[A]): Future[Result] = {
    // Convert to marker context and use request in block
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(request)

    val future = request.jwtSession.getAs[UserLoggedIn]("user") match {
      case Some(userLoggedIn) => {
        block(new AppRequest(Some(userLoggedIn), request, messagesApi))
      }
      case None => block(new AppRequest(None,request, messagesApi))
    }

    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other =>
          result
      }
    }
  }
}
class AppActionBuilderAuthenticated @Inject()(messagesApi: MessagesApi,
                                               playBodyParsers: PlayBodyParsers,
                                               defaultActionBuilder: DefaultActionBuilder,
                                              config: Config)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[AuthenticatedAppRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type UserRequestBlock[A] = AuthenticatedAppRequest[A] => Future[Result]
  private val logger = Logger(this.getClass)
  override def invokeBlock[A](request: Request[A],
                              block: UserRequestBlock[A]): Future[Result] = {

    def notLoggedIn(implicit req: Request[A]) =  Future.successful(
      Forbidden(Json.toJson(ApplicationResult("Unknown", false, "Do login first", None)
    )))

    // Convert to marker context and use request in block
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(request)
    val future: Future[Result] = {
      request.jwtSession.getAs[UserLoggedIn]("user") match {
        case Some(userLoggedIn) => {
            block(new AuthenticatedAppRequest(userLoggedIn, request, messagesApi)).map(_.withJwtSession(jwt.generate(config.apiHost, userLoggedIn)))
        }
        case None => notLoggedIn(request)
      }
    }

    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other =>
          result
      }
    }
  }
}



case class ApplicationResult(reqType:String, success:Boolean, message:String, data:Option[JsValue]){
  def toJsValue = Json.toJson(this)
}
object ApplicationResult{
  implicit val applicationResultWrites:Writes[ApplicationResult] = Json.writes[ApplicationResult]
  implicit def toJsValue(ar: ApplicationResult): JsValue = Json.toJson(ar)
}
