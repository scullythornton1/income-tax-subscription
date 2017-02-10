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
import connectors.SubscriptionETMPConnector
import models.{ErrorModel, IncomeSourcesModel, PropertySubscriptionResponseModel}
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import services.PropertySubscriptionService
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import unit.connectors.mocks.MockHttp
import play.api.http.Status._
import utils.TestConstants

import scala.util.Right

class PropertySubscriptionServiceSpec extends UnitSpec with OneAppPerSuite with MockHttp {

  implicit val hc = HeaderCarrier()
  lazy val httpPost: HttpPost = mockHttpPost
  lazy val config: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val serviceUrl = config.desURL
  val propertySubscriptionSuccessResponse = Json.parse(TestConstants.propertySubscriptionSuccessResponse)
  val propertySubscriptionFailureResponse = TestConstants.propertySubscriptionFailureResponse

  object MockSubscriptionConnector extends SubscriptionETMPConnector(httpPost, config)
  object TestSubscription extends PropertySubscriptionService(MockSubscriptionConnector)

  def setupMockSubscription(nino: String)(status: Int, response: JsValue): Unit =
    setupMockHttpPostEmpty(url = Some(s"$serviceUrl/income-tax-self-assessment/nino/$nino/properties"))(status, response)

  def call = await(TestSubscription.subscribe(nino = "AB12345678A"))

  "PropertySubscriptionService" should {
    "return success if the subscription succeeds" in {
      setupMockSubscription("AB12345678A")(OK, propertySubscriptionSuccessResponse)
      val expected = PropertySubscriptionResponseModel(
        safeId = "XA0001234567890", mtditId = "mdtitId001", incomeSource = IncomeSourcesModel(incomeSourceId = "sourceId0001"))
      val actual = call
      actual shouldBe Right(expected)
    }

    "return not found response" in {
      val reason = "The remote endpoint has indicated that no data can be found."
      val code = "NOT_FOUND_NINO"
      setupMockSubscription("AB12345678A")(NOT_FOUND, Json.parse(propertySubscriptionFailureResponse(code, reason)))
      val expected = ErrorModel(NOT_FOUND, Some(code), reason)
      val actual = call
      actual shouldBe Left(expected)
    }
  }

}
