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
import models.{IncomeSourcesModel, PropertySubscriptionFailureModel, PropertySubscriptionResponseModel}
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import play.api.http.Status._
import utils.Implicits._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionETMPConnectorSpec extends MockitoSugar with UnitSpec with OneAppPerSuite {

  lazy val config: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val http: HttpPost = mockHttpPost

  implicit val hc = HeaderCarrier()

  val mockHttpPost = mock[HttpPost]

  def setupMockHttpPost(url: Option[String] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(Matchers.any[String]())(x => Matchers.eq(x))
    when(mockHttpPost.POSTEmpty[HttpResponse](urlMatcher
    )(Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(status, Some(response))))
  }

  object TestSubscriptionETMPConnector extends SubscriptionETMPConnector(http, config)

  def call = await(TestSubscriptionETMPConnector.subscribePropertyEtmp("AB12345678A"))

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

    "parse and return Invalid Payload response" in {
      val reason = "Submission has not passed validation. Invalid PAYLOAD."
      val code = "INVALID_PAYLOAD"
      val jsFailure = Json.parse(failureResponse(code, reason))
      setupMockHttpPost()(BAD_REQUEST, jsFailure)
      val expected = PropertySubscriptionFailureModel(code, reason)
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return Invalid Nino response" in {
      val reason = "Submission has not passed validation. Invalid parameter NINO."
      val code = "INVALID_NINO"
      val jsFailure = Json.parse(failureResponse(code, reason))
      setupMockHttpPost()(BAD_REQUEST, jsFailure)
      val expected = PropertySubscriptionFailureModel(code, reason)
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return not found response" in {
      val reason = "The remote endpoint has indicated that no data can be found."
      val code = "NOT_FOUND_NINO"
      val jsFailure = Json.parse(failureResponse(code, reason))
      setupMockHttpPost()(NOT_FOUND, jsFailure)
      val expected = PropertySubscriptionFailureModel(code, reason)
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return server error response" in {
      val reason = "DES is currently experiencing problems that require live service intervention."
      val code = "SERVER_ERROR"
      val jsFailure = Json.parse(failureResponse(code, reason))
      setupMockHttpPost()(INTERNAL_SERVER_ERROR, jsFailure)
      val expected = PropertySubscriptionFailureModel(code, reason)
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return service unavailable response" in {
      val reason = "Dependent systems are currently not responding."
      val code = "SERVICE_UNAVAILABLE"
      val jsFailure = Json.parse(failureResponse(code, reason))
      setupMockHttpPost()(SERVICE_UNAVAILABLE, jsFailure)
      val expected = PropertySubscriptionFailureModel(code, reason)
      val actual = call
      actual shouldBe Left(expected)
    }

  }


}
