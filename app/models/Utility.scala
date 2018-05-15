package models

import java.util.Date

import play.api.libs.json.{Json, OFormat}

case class TimePeriod(from: Date, ro: Date)

object UtilityFormats {
  implicit val timePeriodToJson: OFormat[TimePeriod] = Json.format[TimePeriod]
}