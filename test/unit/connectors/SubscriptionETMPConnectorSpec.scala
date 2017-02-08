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

package unit.connectors

import config.AppConfig
import connectors.SubscriptionETMPConnector
import models.{IncomeSourcesModel, PropertySubscriptionFailureModel, PropertySubscriptionRequestModel, PropertySubscriptionResponseModel}
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import play.api.http.Status._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionETMPConnectorSpec extends MockitoSugar with UnitSpec with OneAppPerSuite {

  lazy val config: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val http: HttpPost = mockHttpPost

  implicit val hc = HeaderCarrier()

  val mockHttpPost = mock[HttpPost]

  def setupMockHttpPost[I](url: Option[String] = None, body: Option[I] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(Matchers.any[String]())(x => Matchers.eq(x))
    lazy val bodyMatcher = body.fold(Matchers.any[I]())(x => Matchers.eq(x))
    when(mockHttpPost.POST[I, HttpResponse](urlMatcher, bodyMatcher, Matchers.any()
    )(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(status, Some(response))))
  }

  object TestSubscriptionETMPConnector extends SubscriptionETMPConnector(http, config)

  val request = PropertySubscriptionRequestModel("john@123.com")

  def call = await(TestSubscriptionETMPConnector.subscribePropertyEtmp("AB12345678A", subscribeRequest = request))

  val successResponse =
    """
      |{
      |
      |"safeId": "XA0001234567890",
      |
      |"mtditId": "mdtitId001",
      |
      |"incomeSource":
      |
      |{
      |
      |"incomeSourceId": "sourceId0001"
      |
      |}
      |
      |}
    """.stripMargin

  val failureResponse: (String, String) => String = (code: String, reason: String) =>
    s"""
       |{
       |
       |"code": "$code",
       |
       |"reason": "$reason"
       |
       |}
    """.stripMargin
  val jsSuccess = Json.parse(successResponse)

  "SubscriptionETMPConnector.subscribePropertyEtmp" should {
    "parse and return success response" in {

      setupMockHttpPost()(OK, jsSuccess)
      val expected = PropertySubscriptionResponseModel(safeId = "XA0001234567890", mtditId = "mdtitId001", incomeSource = IncomeSourcesModel(incomeSourceId = "sourceId0001"))
      val actual = call
      actual shouldBe Right(expected)
    }

    "parse and return Bad request response" in {
      val reason = "Submission has not passed validation. Invalid PAYLOAD."
      val code = "INVALID_PAYLOAD"
      val jsFailure = Json.parse(failureResponse(code, reason))
      setupMockHttpPost()(BAD_REQUEST, jsFailure)
      val expected = PropertySubscriptionFailureModel(code, reason)
      val actual = call
      actual shouldBe Left(expected)
    }

  }


}
