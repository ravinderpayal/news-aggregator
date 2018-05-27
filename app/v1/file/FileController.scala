package v1.file

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.{Date, UUID}

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.stream.IOResult
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json._
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import scrapper.{CrawlerSupervisor, NewUrl, ScrapManager}
import utils.ApplicationResult

import scala.concurrent.{ExecutionContext, Future}

case class LoginFormInput(id: String, password: String)
case class RegistrationFormInput(firstName: String, middleName: String, lastName: String, email: String, password: String, dob: Date, gender: String)


/**
  *  Name:- UserController
  *  Jobs:- Login, Registration, Logout, User Search, User Profile
  *
*/
class FileController @Inject()(cc: FileControllerComponents, crawlerSupervisor: CrawlerSupervisor)(implicit ec: ExecutionContext)
    extends FileBaseController(cc) {

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  private val logger = Logger(getClass)
  /**
    * A generic operation on the temporary file that deletes the temp file after completion.
    */
  private def operateOnTempFile(file: File) = {
    val size = Files.size(file.toPath)
    logger.info(s"size = ${size}")
    Files.deleteIfExists(file.toPath)
    size
  }


  def get(id:String): Action[AnyContent] = FileAction.async{implicit request =>
    logger.trace("file get request")
    fileResourceHandler.get(id)
  }

  def upload = Action(parse.temporaryFile).async(parse.temporaryFile) {
    implicit request =>
        val bufferedSource = scala.io.Source.fromFile(request.body)
        for (line <- bufferedSource.getLines) {
          val cols = line.split(",").map(_.trim)
          // do whatever you want with the columns here
          //println(s"${cols(0)}|${cols(1)}|${cols(2)}|${cols(3)}")
          crawlerSupervisor.scrapperActor ! NewUrl(cols(0))
        }
        bufferedSource.close
        Future.successful(Redirect(v1.enterurl.routes.EnterUrlController.index))
  }
}

