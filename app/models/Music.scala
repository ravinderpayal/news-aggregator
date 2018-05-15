package models

import play.api.libs.json.{Json, OFormat}

case class MusicSinger(name: String, facebookId: String)
case class MusicMeta(singer: MusicSinger, length: Int)


object MusicFormats {
  implicit val musicSingerToJson: OFormat[MusicSinger] = Json.format[MusicSinger]
  implicit val musicMetaToJson: OFormat[MusicMeta] = Json.format[MusicMeta]
}
