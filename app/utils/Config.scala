package utils

import javax.inject.{Inject, Singleton}
import play.api.{Application, Configuration}

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps


@Singleton
class Config @Inject()(configuration: Configuration) {
  implicit class ConfigStr(s: String) {
    //(implicit app: Application)
    def configOrElse(default: FiniteDuration): FiniteDuration =
      configuration.getMillis(s).minutes

    def configOrElse(default: Long): Long =
      configuration.getMillis(s) //.getOrElse(default)

    def configOrElse(default: Double)(implicit app: Application): Double =
      configuration.get[Double](s) //.getOrElse(default)
    //(implicit app: Application)
    def configOrElse(default: String): String =
      configuration.get[String](s) // .getOrElse(default)

    def configOrElse(default: Boolean)(implicit app: Application): Boolean =
      configuration.get[Boolean](s) // .getOrElse(default)

    def configOrElse(default: Seq[String])(implicit app: Application): Seq[String]  =
      configuration.get[Seq[String]](s) //.getOrElse(default)
  }


  // ------Application Details

  /**
    * Json Web Token Account Verification Secret
    */
    // (implicit app: Application)
  // def jwtAVsecret: String = "application.jwtToken.accountVerification" configOrElse "sdfsdfef3233e2"

  // def jwtAuthenticationSecret: String = "application.jwtToken.authentication" configOrElse "sdfsdyfreaf3233e23d"
  // def jwtAdminAuthenticationSecret: String = "application.admin.jwtToken.authentication" configOrElse "sdfsdyfreaf3kkhkjhjkh233e23d"

  // def emailVerificationResend: String = "application.jwtToken.emailVerificationResend" configOrElse "sdssderfeyffErfef3d"
  def perOpinionFees = 99

  def apiHost: String = "application.apiHost" configOrElse "localhost"
  def apiHostProtocol: String = "application.apiHostProtocol" configOrElse "http://"
  def apiHostPort: String = "application.apiHostPort" configOrElse "9000"

  def operatingEntity: String = "application.operatingEntity" configOrElse apiHost

  def website: String = "application.website" configOrElse("http://" concat apiHost)


  def croneInterval: FiniteDuration = "application.croneInterval" configOrElse(5 minutes)


  // def openTokApiKey: Long = "application.openTok.apiKey" configOrElse 0
  // def openTokApiSecret: String = "application.openTok.apiSecret" configOrElse "not_available"
}
