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

import audit.Logging
import connectors.RegistrationConnector
import models.ErrorModel
import models.registration._
import org.scalatestplus.play.OneAppPerSuite
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}
import uk.gov.hmrc.play.test.UnitSpec
import unit.connectors.mocks.MockHttp
import utils.JsonUtils._
import utils.TestConstants

class RegistrationConnectorSpec extends UnitSpec with MockHttp with OneAppPerSuite {

  lazy val config: Configuration = app.injector.instanceOf[Configuration]
  lazy val logging: Logging = app.injector.instanceOf[Logging]
  lazy val httpPost: HttpPost = mockHttpPost
  lazy val httpGet: HttpGet = mockHttpGet

  object TestRegistrationConnector extends RegistrationConnector(config, logging, httpPost, httpGet)

  implicit val hc = HeaderCarrier()

  val individual = IndividualModel("f", "l")
  val register = RegistrationRequestModel(isAnAgent = false, individual = individual)
  val nino: String = TestConstants.testNino
  val safeId: String = TestConstants.testSafeId

  "RegistrationConnector.register" should {
    "Put in the correct headers" in {
      val rHc = TestRegistrationConnector.createHeaderCarrierPost(hc)
      rHc.headers.contains("Authorization" -> s"Bearer ${config.getString("microservice.services.registration.authorization-token").get}") shouldBe true
      rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
      rHc.headers.contains("Environment" -> config.getString("microservice.services.registration.environment").get) shouldBe true
    }

    "Post to the correct url" in {
      TestRegistrationConnector.newRegistrationUrl(nino) should endWith(s"/registration/individual/NINO/$nino")
    }

    import TestConstants.NewRegistrationResponse._

    lazy val expectedUrl = TestRegistrationConnector.newRegistrationUrl(nino)

    def call = await(TestRegistrationConnector.register(nino, register))

    "parse and return the success response correctly" in {
      setupMockHttpPost(url = expectedUrl)(OK, successResponse(safeId))
      val expected = RegistrationSuccessResponseModel(safeId)
      val actual = call
      actual shouldBe Right(expected)
    }

    "parse and return the Bad request response correctly" in {
      val reason = "Your submission contains one or more errors. Failed Parameter(s) - [idType, idNumber, payload]"
      val code = "INVALID_NINO"
      setupMockHttpPost(url = expectedUrl)(BAD_REQUEST, failureResponse(code, reason))
      val expected = ErrorModel(BAD_REQUEST, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Resource not found response correctly" in {
      val reason = "Resource not found"
      val code = "NOT_FOUND"
      setupMockHttpPost(url = expectedUrl)(NOT_FOUND, failureResponse("NOT_FOUND", reason))
      val expected = ErrorModel(NOT_FOUND, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Conflict error response correctly" in {
      val reason = "Duplicate submission"
      val code = "CONFLICT"
      setupMockHttpPost(url = expectedUrl)(CONFLICT, failureResponse(code, reason))
      val expected = ErrorModel(CONFLICT, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Server error response correctly" in {
      val reason = "Server Error"
      val code = "SERVER_ERROR"
      setupMockHttpPost(url = expectedUrl)(INTERNAL_SERVER_ERROR, failureResponse(code, reason))
      val expected = ErrorModel(INTERNAL_SERVER_ERROR, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Service unavailable response correctly" in {
      val reason = "Service unavailable"
      val code = "SERVICE_UNAVAILABLE"
      setupMockHttpPost(url = expectedUrl)(SERVICE_UNAVAILABLE, failureResponse(code, reason))
      val expected = ErrorModel(SERVICE_UNAVAILABLE, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      setupMockHttpPost(url = expectedUrl)(INTERNAL_SERVER_ERROR, corruptResponse)
      val expected = ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse))
      val actual = call
      actual shouldBe Left(expected)
    }
  }

  "RegistrationConnector.getRegistration" should {
    "Put in the correct headers" in {
      val rHc = TestRegistrationConnector.createHeaderCarrierGet(hc)
      rHc.headers.contains("Authorization" -> s"Bearer ${config.getString("microservice.services.registration.authorization-token").get}") shouldBe true
      rHc.headers.contains("Environment" -> config.getString("microservice.services.registration.environment").get) shouldBe true
    }

    "Post to the correct url" in {
      TestRegistrationConnector.getRegistrationUrl(nino) should endWith(s"/registration/details?nino=$nino")
    }
    import TestConstants.GetRegistrationResponse._

    lazy val expectedUrl = TestRegistrationConnector.getRegistrationUrl(nino)

    def call = await(TestRegistrationConnector.getRegistration(nino))

    "parse and return the success response correctly" in {
      setupMockHttpGet(url = expectedUrl)(OK, successResponse(safeId))
      val expected = RegistrationSuccessResponseModel(safeId)
      val actual = call
      actual shouldBe Right(expected)
    }

    "parse and return the Bad request response correctly" in {
      val reason = "Your submission contains one or more errors. Failed Parameter(s) - [idType, idNumber, payload]"
      setupMockHttpGet(url = expectedUrl)(BAD_REQUEST, failureResponse(reason))
      val expected = ErrorModel(BAD_REQUEST, GetRegistrationFailureResponseModel(reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Resource not found response correctly" in {
      val reason = "Resource not found"
      setupMockHttpGet(url = expectedUrl)(NOT_FOUND, failureResponse(reason))
      val expected = ErrorModel(NOT_FOUND, GetRegistrationFailureResponseModel(reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Server error response correctly" in {
      val reason = "Server Error"
      setupMockHttpGet(url = expectedUrl)(INTERNAL_SERVER_ERROR, failureResponse(reason))
      val expected = ErrorModel(INTERNAL_SERVER_ERROR, GetRegistrationFailureResponseModel(reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Service unavailable response correctly" in {
      val reason = "Service unavailable"
      setupMockHttpGet(url = expectedUrl)(SERVICE_UNAVAILABLE, failureResponse(reason))
      val expected = ErrorModel(SERVICE_UNAVAILABLE, GetRegistrationFailureResponseModel(reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      setupMockHttpGet(url = expectedUrl)(INTERNAL_SERVER_ERROR, corruptResponse)
      val expected = ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse))
      val actual = call
      actual shouldBe Left(expected)
    }
  }

}
