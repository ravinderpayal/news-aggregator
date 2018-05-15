package models

import java.util.Date

import play.api.libs.json.{Json, OFormat}

// todo: in V2 add more attributes
case class MovieMeta(releasedOn: Option[Date])

object MovieFormats {
  implicit val movieMetaToJson: OFormat[MovieMeta] = Json.format[MovieMeta]
}
