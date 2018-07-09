package v1.enterurl

import java.util.UUID

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import utils.extractors._
/**
  * Routes and URLs to the Notification controller.
  */
class EnterUrlRouter @Inject()(controller: EnterUrlController) extends SimpleRouter {
  val prefix = "/api/v1/notification"

  override def routes: Routes = {
   case GET(p"/") =>
      controller.index
    //case POST(p"/mark/seen") =>
    //  controller.markSeen
    //case GET(p"/get/latest/${int(counter)}") =>
    //  controller.getLatest(counter)
    case GET(p"/${int(id)}") =>
      controller.get(id)
  }

}
