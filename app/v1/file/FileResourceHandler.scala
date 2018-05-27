package v1.file

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import javax.inject.{Inject, Provider}
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Configuration, MarkerContext, _}
import utils.ApplicationResult

import scala.concurrent.{ExecutionContext, Future}


/**
  * Controls access to and creation of the backend data
  */
class FileResourceHandler @Inject()(
                                     routerProvider: Provider[FileRouter],
                                     fileRepository: FileRepositoryImpl,
                                     config: Configuration)(implicit ec: ExecutionContext) {
  private val logger = Logger(this.getClass)


  /**
    * Uploads a multipart file as a POST request.
    *
    * @return
    */
  def create(input: FileData)(implicit mc: MarkerContext): Future[Result] = {
          fileRepository.create(input).map (_ match {
                case Some(obj) => if (obj) {
                  Created(Json.toJson(ApplicationResult(
                      "FileUpload",
                      true,
                      "File Uploaded Successfully",
                    Some(Json.toJson(input))
                    )))
                } else InternalServerError(
                  Json.toJson(ApplicationResult(
                    "Registration",
                    false,
                    "Error with Server. Please try after some time",
                    None))
                )

                case None => InternalServerError(
                  Json.toJson(ApplicationResult(
                    "Registration",
                    false,
                    "Error with Server. Please try after some time",
                    None))
                )
              })
  }

  def get(id:String) = {
    fileRepository.get(id).map(_ match {
        case Some(obj) => {
          val file = new java.io.File(obj.tempPath)
          val path: java.nio.file.Path = file.toPath
          val source: Source[ByteString, _] = FileIO.fromPath(path)

          Result(
            header = ResponseHeader(200, Map.empty),
            body = HttpEntity.Streamed(source, None, Some(obj.meta.contentType))
          )

           //   Ok.sendFile(new java.io.File(obj.tempPath))
        }
        case None => NotFound(Json.toJson(
          ApplicationResult("File", false, "File Not Found", None)
        ))
      })
  }
}