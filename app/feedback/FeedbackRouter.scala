package feedback

import java.util.UUID

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import utils.extractors.uuid

/**
  * Routes and URLs to the PostResource controller.
  */
class FeedbackRouter @Inject()(controller: FeedbackController) extends SimpleRouter {

  override def routes: Routes = {
    case POST(p"/") =>
      controller.save()
    case GET(p"/${uuid(id)}") =>
      controller.get(id)
  }

}
