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
import connectors.SubscriptionConnector
import models.subscription.business.BusinessSubscriptionRequestModel
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.HttpPost
import utils.JsonUtils._
import utils.TestConstants.{BusinessSubscriptionResponse, PropertySubscriptionResponse, _}

trait MockSubscriptionConnector extends MockHttp with GuiceOneAppPerSuite {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val httpPost: HttpPost = mockHttpPost
  lazy val logging: Logging = app.injector.instanceOf[Logging]

  val mockPropertySubscribe = (setupMockPropertySubscribe(testNino) _).tupled

  def mockBusinessSubscribe(payload: BusinessSubscriptionRequestModel) = (setupMockBusinessSubscribe(testNino, payload) _).tupled

  val propertySubscribeSuccess = (OK, PropertySubscriptionResponse.successResponse(testSafeId, testMtditId, testSourceId))
  val businessSubscribeSuccess = (OK, BusinessSubscriptionResponse.successResponse(testSafeId, testMtditId, testSourceId))

  object TestSubscriptionConnector extends SubscriptionConnector(appConfig, httpPost, logging)

  def setupMockBusinessSubscribe(nino: String, payload: BusinessSubscriptionRequestModel)(status: Int, response: JsValue): Unit =
    setupMockHttpPost(url = TestSubscriptionConnector.businessSubscribeUrl(nino), payload)(status, response)

  def setupMockPropertySubscribe(nino: String)(status: Int, response: JsValue): Unit =
    setupMockHttpPost(url = TestSubscriptionConnector.propertySubscribeUrl(nino), optionUtl("{}": JsValue))(status, response)

}
