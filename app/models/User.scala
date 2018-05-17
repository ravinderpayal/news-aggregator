package models

import java.util.{Date, UUID}

import models.Gender.Gender
import models.LoginProvider.LoginProvider
import play.api.libs.json._

case class UserName(firstName: String, middleName: Option[String], lastName: String)

// todo: Make place hold more things
case class EducationInstitution(name: String, place: String)

case class UserEducation(institution: EducationInstitution, period: Option[TimePeriod])

case class UserInterests(name: String, createdAt: Date, iType: Option[String], facebookId: Option[String])

case class UserMusic(name: String, musicMeta: Option[MusicMeta])

case class UserMovies(name: String, meta: Option[MovieMeta])
case class UserTvSeries(name: String, meta: Option[MovieMeta])

case class UserMeta(bio: Option[String], education: List[UserEducation], interests: List[UserInterests], music: List[UserMusic], movies: List[UserMovies], tvSeries: List[UserTvSeries])

object Gender extends Enumeration {
  def apply(gender: String): Gender = if (gender == "Male") Gender.Female else if (gender == "Female") Gender.Female else Gender.Others
  type Gender = Value
  val Female, Male, Others = Value
  implicit val genderReads = Reads.enumNameReads(Gender)
  implicit val genderWrites = Writes.enumNameWrites
}

case class AppUserProfile(name: UserName, picture: Option[String], dob:Option[Date]) {
  def toJsValue = Json.toJson(this)
  override def toString = Json.prettyPrint(toJsValue)
}

case class AppUser(
              id: UUID,
              facebookId: Option[String],
              email: String,
              mobileNumber: String,
              password: Option[String],
              gender: Gender,
              profile: AppUserProfile) {
  def toUserLoggedIn(loginProvider: LoginProvider) = UserLoggedIn(id, email, mobileNumber, loginProvider, utils.util.getAge(profile.dob.get), gender)
  def toJsValue = Json.toJson(this)
  override def toString = {
    val ret = Json.prettyPrint(toJsValue)
    println(ret)
    ret
  }
}


object UserFormats {
  import MusicFormats.{musicMetaToJson, musicSingerToJson}
  import MovieFormats.{movieMetaToJson}
  import UtilityFormats.timePeriodToJson
  implicit val userNameToJson: OFormat[UserName] = Json.format[UserName]
  implicit val educationInstitutionToJson: OFormat[EducationInstitution] = Json.format[EducationInstitution]
  implicit val userEducationToJson: OFormat[UserEducation] = Json.format[UserEducation]
  implicit val userInterestsToJson: OFormat[UserInterests] = Json.format[UserInterests]
  implicit val userMusicToJson: OFormat[UserMusic] = Json.format[UserMusic]
  implicit val userMoviesToJson: OFormat[UserMovies] = Json.format[UserMovies]
  implicit val userTvSeriesToJson: OFormat[UserTvSeries] = Json.format[UserTvSeries]
  implicit val userMetaToJson: OFormat[UserMeta] = Json.format[UserMeta]
}
object AppUserProfile{
  import UserFormats._
  implicit val appUserProfileToJson: OFormat[AppUserProfile] = Json.format[AppUserProfile]
}

object AppUser{
  implicit val appUserToJson: OFormat[AppUser] = Json.format[AppUser]
}

object LoginProvider extends Enumeration {
  type LoginProvider = Value
  val Facebook, MobileNumber, Email = Value
  implicit val loginProviderReads = Reads.enumNameReads(LoginProvider)
  implicit val loginProviderWrites = Writes.enumNameWrites
}

case class UserLoggedIn(id: UUID, email: String, mobileNumber: String, provider: LoginProvider, age: Int, gender: Gender)
object UserLoggedIn {
  implicit val jsonFormat:OFormat[UserLoggedIn] = Json.format[UserLoggedIn]
}
