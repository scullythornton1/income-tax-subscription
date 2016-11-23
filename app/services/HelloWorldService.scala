package services

import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future


case class Hello(message: String)

object Hello {
  implicit val format = Json.format[Hello]
}


trait HelloWorldService {

  def fetchWorld(implicit hc: HeaderCarrier): Future[Hello]

  def fetchUser(implicit hc: HeaderCarrier): Future[Hello]

  def fetchApplication(implicit hc: HeaderCarrier): Future[Hello]

}

object LiveService extends HelloWorldService {
  override def fetchWorld(implicit hc: HeaderCarrier): Future[Hello] =
    Future.successful(Hello("Hello World"))

  override def fetchApplication(implicit hc: HeaderCarrier): Future[Hello] =
    Future.successful(Hello("Hello Application"))

  override def fetchUser(implicit hc: HeaderCarrier): Future[Hello] =
    Future.successful(Hello("Hello User"))
}

object SandboxService extends HelloWorldService {
  override def fetchWorld(implicit hc: HeaderCarrier): Future[Hello] =
    Future.successful(Hello("Hello Sandbox World"))

  override def fetchApplication(implicit hc: HeaderCarrier): Future[Hello] =
    Future.successful(Hello("Hello Sandbox Application"))

  override def fetchUser(implicit hc: HeaderCarrier): Future[Hello] =
    Future.successful(Hello("Hello Sandbox User"))
}



