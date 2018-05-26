package v1.enterurl

import java.util.UUID

import javax.inject.{Inject, Singleton}
import models.UserName
import play.api.data.{Form, Forms}
import play.api.data.Forms.{email, mapping, optional, text}
import play.api.libs.json._
import play.api.mvc._

import scrapper.JobManager

import scala.concurrent.{ExecutionContext, Future}


/**
  *  Name:- AskOpinionController
  *  Jobs:- Get Latest, Get Specific, Mark Seen
  *
*/
@Singleton
class EnterUrlController @Inject()(cc: EnterUrlControllerComponents, jobManager: JobManager)(implicit ec: ExecutionContext)
  extends EnterUrlBaseController(cc) with play.api.i18n.I18nSupport {

  def get(link: String): Action[AnyContent] = Action.async { implicit request =>
    //askOpinionResourceHandler.get(request.user.id, id)
    jobManager.get(link).map(ax => Ok(views.html.showimages(ax)))
  }

  def getLatest(counter: Int): Action[AnyContent] = ActionAuthenticated { implicit request =>
    // askOpinionResourceHandler.getLatest(request.user.id, counter)
    Ok
  }

  def enter(link:String) = Action {//again..can't your unplug the keyboard for once, i can't even click
    Future{
      jobManager.scrap(link)
    }
    Redirect(v1.enterurl.routes.EnterUrlController.index)
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
    jobManager.get.map(x=>
    Ok(views.html.index(x)))
  }

  def annotate(imgId: String, annotation: String) = Action.async{
    implicit request =>
      jobManager.annotate(imgId.toInt, annotation) map (_=>Ok)
  }

  def blankImage(imgId: String) = Action.async{
    implicit request =>
      jobManager.blankImage(imgId.toInt) map (_=>Ok)
  }

  def countImageAnnotations() = Action.async{
    implicit request =>
      jobManager.countImageAnnotations map (a => Ok(a.toString))

  }

}


