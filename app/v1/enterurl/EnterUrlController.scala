package v1.enterurl

import java.util.UUID

// import controllers.{routes}
import javax.inject.{Inject, Singleton}
import models.UserName
import play.api.data.{Form, Forms}
import play.api.data.Forms.{email, mapping, optional, text}
import play.api.libs.json._
import play.api.mvc.Results.Ok
import play.api.mvc._
import scrapper.{CrawlerSupervisor, NewUrl, ScrapManager}

import scala.concurrent.{ExecutionContext, Future}


case class UserLoginForm(email:String, password: String)

/**
  *  Name:- EnterUrlController
  *  Jobs:- CRUD
  *
*/
@Singleton
class EnterUrlController @Inject()(cc: EnterUrlControllerComponents, crawlerSupervisor: CrawlerSupervisor, scrapManager: ScrapManager)(implicit ec: ExecutionContext)
  extends EnterUrlBaseController(cc) with play.api.i18n.I18nSupport {

  private def loadTop()(cb: Seq[(Int, String, String, String, String)] => Result): Unit = {
    scrapManager.get(id).map(ax => {

    })
  }
  def get(id: Int): Action[AnyContent] = Action.async { implicit request =>
    //askOpinionResourceHandler.get(request.user.id, id)
    scrapManager.get(id).map(ax => {
      if (ax.length > 0)
        Ok(views.html.blog(ax.head._2,ax.head._5,ax.head._3,ax.head._2))
      else NotFound("404 not found")
    })
  }

  def getLatest(counter: Int): Action[AnyContent] = ActionAuthenticated { implicit request =>
    // askOpinionResourceHandler.getLatest(request.user.id, counter)
    Ok
  }

  def enter(link:String, password:String) = Action {//again..can't your unplug the keyboard for once, i can't even click
    if (password == "helloislam") {
      crawlerSupervisor.scrapperActor ! NewUrl(link)
      Redirect(v1.enterurl.routes.EnterUrlController.index)
    } else {
      Forbidden("403:You are not authorized")
    }
  }


  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  // async means it won't wait for the actual result from database, this method will return a wrapper, and that wrapper will have a reference to result, basically a technique for designing non blocking apps
  // ignore for now
  def index = Action.async { implicit request =>
    scrapManager.get(0, 10).map(x=>
      Ok(views.html.index(x))
    )
  }


  def countArticles() = Action.async{
    implicit request =>
     scrapManager.countArticles map (a => Ok(a.toString))

  }

}


