package v1.file

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the PostResource controller.
  */
class FileRouter @Inject()(controller: FileController) extends SimpleRouter {
  override def routes: Routes = {
   /* case GET(p"/") =>
      controller.index*/
    case POST(p"/upload") =>
      controller.upload
    case GET(p"/get/$id") =>
      controller.get(id)
  }

}
