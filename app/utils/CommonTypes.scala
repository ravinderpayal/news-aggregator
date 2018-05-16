package utils

import play.api.libs.json._

case class Money(amount:Int, currency:String)
object Money{
  implicit val moneyFormat:OFormat[Money] = Json.format[Money]
}
object Currencies{
  val USD  = 1
  val INR = 2
  val BTC = 3
}