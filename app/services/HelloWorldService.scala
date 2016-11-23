package services

import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.Future
import models.HelloWorldModel


trait HelloWorldService {

  def fetchWorld(implicit hc: HeaderCarrier): Future[HelloWorldModel]

  def fetchUser(implicit hc: HeaderCarrier): Future[HelloWorldModel]

  def fetchApplication(implicit hc: HeaderCarrier): Future[HelloWorldModel]

}

object LiveHelloWorldService extends HelloWorldService {
  override def fetchWorld(implicit hc: HeaderCarrier): Future[HelloWorldModel] =
    Future.successful(HelloWorldModel("Hello World"))

  override def fetchApplication(implicit hc: HeaderCarrier): Future[HelloWorldModel] =
    Future.successful(HelloWorldModel("Hello Application"))

  override def fetchUser(implicit hc: HeaderCarrier): Future[HelloWorldModel] =
    Future.successful(HelloWorldModel("Hello User"))
}

object SandboxHelloWorldService extends HelloWorldService {
  override def fetchWorld(implicit hc: HeaderCarrier): Future[HelloWorldModel] =
    Future.successful(HelloWorldModel("Hello Sandbox World"))

  override def fetchApplication(implicit hc: HeaderCarrier): Future[HelloWorldModel] =
    Future.successful(HelloWorldModel("Hello Sandbox Application"))

  override def fetchUser(implicit hc: HeaderCarrier): Future[HelloWorldModel] =
    Future.successful(HelloWorldModel("Hello Sandbox User"))
}



