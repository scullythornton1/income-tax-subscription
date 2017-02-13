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

package unit.connectors.mocks

import config.AppConfig
import connectors.SubscriptionConnector
import models.subscription.business.BusinessSubscriptionRequestModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.Configuration
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.HttpPost
import utils.Implicits._

trait MockSubscriptionConnector extends MockHttp with OneAppPerSuite {

  lazy val config: Configuration = app.injector.instanceOf[Configuration]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val httpPost: HttpPost = mockHttpPost

  object TestSubsscriptionConnector extends SubscriptionConnector(config, httpPost, appConfig)

  def setupMockBusinessSubscribe(nino: String, payload: BusinessSubscriptionRequestModel)(status: Int, response: JsValue): Unit =
    setupMockHttpPost(url = TestSubsscriptionConnector.businessSubscribeUrl(nino), payload)(status, response)

  def setupMockPropertySubscribe(nino: String)(status: Int, response: JsValue): Unit =
    setupMockHttpPostEmpty(url = TestSubsscriptionConnector.propertySubscribeUrl(nino))(status, response)

}
