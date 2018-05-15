package utils

import java.util.UUID

import play.api.mvc.PathBindable.Parsing
import play.api.routing.sird.PathBindableExtractor

object extractors {
  implicit object bindableUUID extends Parsing[UUID](
    UUID.fromString,
    _.toString,
    (key: String, e: Exception) => s"$key is not a valid UUID"
  )
  val uuid = new PathBindableExtractor[UUID]
}
