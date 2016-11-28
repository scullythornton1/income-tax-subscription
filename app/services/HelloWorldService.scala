/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
