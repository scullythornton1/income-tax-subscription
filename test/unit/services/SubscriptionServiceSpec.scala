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

package unit.services

import config.AppConfig
import connectors.SubscriptionConnector
import models.{ErrorModel, IncomeSourcesModel, PropertySubscriptionResponseModel}
import org.scalatestplus.play.OneAppPerSuite
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import services.SubscriptionService
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}
import uk.gov.hmrc.play.test.UnitSpec
import unit.connectors.mocks.MockHttp
import play.api.http.Status._
import utils.{JsonUtils, TestConstants}

import scala.util.Right

class SubscriptionServiceSpec extends UnitSpec with OneAppPerSuite with MockHttp with JsonUtils {

  implicit val hc = HeaderCarrier()

  lazy val config: Configuration = app.injector.instanceOf[Configuration]
  lazy val httpPost: HttpPost = mockHttpPost
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  lazy val serviceUrl = appConfig.desURL

  object MockSubscriptionConnector extends SubscriptionConnector(config, httpPost, appConfig)
  object TestSubscription extends SubscriptionService(MockSubscriptionConnector)

  "SubscriptionService" should {

    def setupMockPropertySubscription(nino: String)(status: Int, response: JsValue): Unit =
      setupMockHttpPostEmpty(url = Some(s"$serviceUrl/income-tax-self-assessment/nino/$nino/properties"))(status, response)

    def propertySubscribeCall = await(TestSubscription.propertySubscribe(nino = "AB12345678A"))

    "return success if the subscription succeeds" in {

      setupMockPropertySubscription("AB12345678A")(OK, TestConstants.propertySubscriptionSuccessResponse)
      val expected = PropertySubscriptionResponseModel(
        safeId = "XA0001234567890", mtditId = "mdtitId001", incomeSource = IncomeSourcesModel(incomeSourceId = "sourceId0001"))
      propertySubscribeCall shouldBe Right(expected)
    }

    "return not found response" in {
      val reason = "The remote endpoint has indicated that no data can be found."
      val code = "NOT_FOUND_NINO"
      setupMockPropertySubscription("AB12345678A")(NOT_FOUND, TestConstants.propertySubscriptionFailureResponse(code, reason))
      val expected = ErrorModel(NOT_FOUND, Some(code), reason)
      propertySubscribeCall shouldBe Left(expected)
    }
  }

}
