package utils

import javax.inject.{Inject, Singleton}
import models.{AppUser, UserName}
import play.api.libs.mailer._
import v1.notification.NotificationData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Email @Inject()(mailerClient: MailerClient, config: Config)(implicit ec: ExecutionContext) {
  /*def sendEmail(email: Email) = {// subject: String, from: String, to: Seq[String], bodyText: Option[String] = None, bodyHtml: Option[String] = None) = {
  }*/
  def verifyAccount(name: UserName, email: String, link: String) = {
    val send = Email(
                  s"Hello Dear ${name.firstName} ${name.lastName}. Please verify your account",
                  "noreply@mandisoft.com",
                  Seq(s"${name.firstName} ${name.lastName}<${email}>"),
                  // adds attachment
                  /*
                  attachments = Seq(
                    AttachmentFile("attachment.pdf", new File("/some/path/attachment.pdf")),
                    // adds inline attachment from byte array
                    AttachmentData("data.txt", "data".getBytes, "text/plain", Some("Simple data"), Some(EmailAttachment.INLINE)),
                    // adds cid attachment
                    AttachmentFile("image.jpg", new File("/some/path/image.jpg"), contentId = Some(cid))
                  ),*/
                  // sends text, HTML or both...
                  bodyText = Some("A text message"),
                  bodyHtml = Some(s"""<html>
                                        <body>
                                          <p>
                                            Hello Dear  ${name.firstName} ${name.lastName}. Please veriy your account by clicking the link below. Ignore if you didn't created an account.
                                            <a href="${link}">Verification Link: ${link}</a>
                                            <br />
                                            <small>Link is valid for 24 hours only</small>
                                          </p>
                                          <footer>
                                            You received this email from <a href="${config.website}">${config.operatingEntity}</a>, because you or some-one else entered your email id in registration form.
                                          </footer>
                                        </body>
                                      </html>"""))
                        Future{
                          println("Sending email")
                          val msgId = mailerClient.send(send)
                          println(msgId)
                        }
  }

  def sendNotification(user: AppUser, notification: NotificationData) = {
    val send = Email(
      notification.title,
      "noreply@mandisoft.com",
      Seq(s"${user.profile.name.firstName} ${user.profile.name.lastName}<${user.email}>"),
      // adds attachment
      /*
      attachments = Seq(
        AttachmentFile("attachment.pdf", new File("/some/path/attachment.pdf")),
        // adds inline attachment from byte array
        AttachmentData("data.txt", "data".getBytes, "text/plain", Some("Simple data"), Some(EmailAttachment.INLINE)),
        // adds cid attachment
        AttachmentFile("image.jpg", new File("/some/path/image.jpg"), contentId = Some(cid))
      ),*/
      // sends text, HTML or both...
      bodyText = Some(notification.text),
      bodyHtml = Some(s"""<html>
                            <body>
                              <p>
                                Hello Dear  ${user.profile.name.firstName} ${user.profile.name.lastName}. You have 1 notification.
                                <br />
                                <ul>
                                  <li>
                                    ${notification.title}
                                    <br />
                                    ${notification.text}
                                  </li>
                                </ul>
                              </p>
                              <footer>
                                You received this email from <a href="${config.website}">${config.operatingEntity}</a>, because you have opted-in for notification.
                              </footer>
                            </body>
                          </html>
        """))
      Future{
        println("Sending email")
        val msgId = mailerClient.send(send)
        println(msgId)
      }
  }
}
