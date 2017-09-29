/*
 * Copyright 2017 HM Revenue & Customs
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

package connectors.mocks

import audit.Logging
import config.AppConfig
import connectors.RegistrationConnector
import models.registration.RegistrationRequestModel
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.JsValue
import utils.Implicits._
import utils.TestConstants.{GetRegistrationResponse, NewRegistrationResponse, _}
import uk.gov.hmrc.http.{HttpGet, HttpPost}
import scala.concurrent.ExecutionContext.Implicits.global

trait MockRegistrationConnector extends MockHttp with GuiceOneAppPerSuite {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val logging: Logging = app.injector.instanceOf[Logging]
  lazy val httpPost: HttpPost = mockHttpPost
  lazy val httpGet: HttpGet = mockHttpGet

  def mockRegister(payload: RegistrationRequestModel) = (setupMockRegister(testNino, payload) _).tupled

  val mockGetRegistration = (setupMockGetRegistration(testNino) _).tupled

  object TestRegistrationConnector extends RegistrationConnector(appConfig, logging, httpPost, httpGet)

  val regSuccess = (OK, NewRegistrationResponse.successResponse(testSafeId))
  val getRegSuccess = (OK, GetRegistrationResponse.successResponse(testSafeId))

  def setupMockRegister(nino: String, payload: RegistrationRequestModel)(status: Int, response: JsValue): Unit =
    setupMockHttpPost(url = TestRegistrationConnector.newRegistrationUrl(nino), payload)(status, response)

  def setupMockGetRegistration(nino: String)(status: Int, response: JsValue): Unit =
    setupMockHttpGet(url = TestRegistrationConnector.getRegistrationUrl(nino))(status, response)
}