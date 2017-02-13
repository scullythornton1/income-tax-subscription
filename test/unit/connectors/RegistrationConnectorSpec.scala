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

import models.ErrorModel
import models.registration._
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.connectors.mocks.MockRegistrationConnector
import utils.JsonUtils._
import utils.TestConstants

class RegistrationConnectorSpec extends MockRegistrationConnector {

  implicit val hc = HeaderCarrier()
  val env = config.getString("microservice.services.des.environment").get
  val authToken = config.getString("microservice.services.des.authorization-token").get

  val register = RegistrationRequestModel(isAnAgent = false)
  val nino: String = TestConstants.testNino
  val safeId: String = TestConstants.testSafeId

  "RegistrationConnector.register" should {
    "Put in the correct headers" in {
      val rHc = TestRegistrationConnector.createHeaderCarrierPost(hc)

      rHc.headers.contains("Authorization" -> s"Bearer $authToken") shouldBe true
      rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
      rHc.headers.contains("Environment" -> env) shouldBe true
    }

    "Post to the correct url" in {
      TestRegistrationConnector.newRegistrationUrl(nino) should endWith(s"/registration/individual/NINO/$nino")
    }

    import TestConstants.NewRegistrationResponse._

    def call = await(TestRegistrationConnector.register(nino, register))

    def setupMockRegister(status: Int, response: JsValue): Unit =
      super.setupMockRegister(nino)(status, response)

    "parse and return the success response correctly" in {
      setupMockRegister(OK, successResponse(safeId))
      val expected = RegistrationSuccessResponseModel(safeId)
      val actual = call
      actual shouldBe Right(expected)
    }

    "parse and return the Bad request response correctly" in {
      val reason = "Your submission contains one or more errors. Failed Parameter(s) - [idType, idNumber, payload]"
      val code = "INVALID_NINO"
      setupMockRegister(BAD_REQUEST, failureResponse(code, reason))
      val expected = ErrorModel(BAD_REQUEST, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Resource not found response correctly" in {
      val reason = "Resource not found"
      val code = "NOT_FOUND"
      setupMockRegister(NOT_FOUND, failureResponse("NOT_FOUND", reason))
      val expected = ErrorModel(NOT_FOUND, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Conflict error response correctly" in {
      val reason = "Duplicate submission"
      val code = "CONFLICT"
      setupMockRegister(CONFLICT, failureResponse(code, reason))
      val expected = ErrorModel(CONFLICT, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Server error response correctly" in {
      val reason = "Server Error"
      val code = "SERVER_ERROR"
      setupMockRegister(INTERNAL_SERVER_ERROR, failureResponse(code, reason))
      val expected = ErrorModel(INTERNAL_SERVER_ERROR, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Service unavailable response correctly" in {
      val reason = "Service unavailable"
      val code = "SERVICE_UNAVAILABLE"
      setupMockRegister(SERVICE_UNAVAILABLE, failureResponse(code, reason))
      val expected = ErrorModel(SERVICE_UNAVAILABLE, NewRegistrationFailureResponseModel(code, reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      setupMockRegister(INTERNAL_SERVER_ERROR, corruptResponse)
      val expected = ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse))
      val actual = call
      actual shouldBe Left(expected)
    }
  }

  "RegistrationConnector.getRegistration" should {
    "Put in the correct headers" in {
      val rHc = TestRegistrationConnector.createHeaderCarrierGet(hc)
      rHc.headers.contains("Authorization" -> s"Bearer $authToken") shouldBe true
      rHc.headers.contains("Environment" -> env) shouldBe true
    }

    "Post to the correct url" in {
      TestRegistrationConnector.getRegistrationUrl(nino) should endWith(s"/registration/details?nino=$nino")
    }
    import TestConstants.GetRegistrationResponse._

    def call = await(TestRegistrationConnector.getRegistration(nino))

    def setupMockGetRegistration(status: Int, response: JsValue): Unit =
      super.setupMockGetRegistration(nino)(status, response)

    "parse and return the success response correctly" in {
      setupMockGetRegistration(OK, successResponse(safeId))
      val expected = RegistrationSuccessResponseModel(safeId)
      val actual = call
      actual shouldBe Right(expected)
    }

    "parse and return the Bad request response correctly" in {
      val reason = "Your submission contains one or more errors. Failed Parameter(s) - [idType, idNumber, payload]"
      setupMockGetRegistration(BAD_REQUEST, failureResponse(reason))
      val expected = ErrorModel(BAD_REQUEST, GetRegistrationFailureResponseModel(reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Resource not found response correctly" in {
      val reason = "Resource not found"
      setupMockGetRegistration(NOT_FOUND, failureResponse(reason))
      val expected = ErrorModel(NOT_FOUND, GetRegistrationFailureResponseModel(reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Server error response correctly" in {
      val reason = "Server Error"
      setupMockGetRegistration(INTERNAL_SERVER_ERROR, failureResponse(reason))
      val expected = ErrorModel(INTERNAL_SERVER_ERROR, GetRegistrationFailureResponseModel(reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Service unavailable response correctly" in {
      val reason = "Service unavailable"
      setupMockGetRegistration(SERVICE_UNAVAILABLE, failureResponse(reason))
      val expected = ErrorModel(SERVICE_UNAVAILABLE, GetRegistrationFailureResponseModel(reason))
      val actual = call
      actual shouldBe Left(expected)
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      setupMockGetRegistration(INTERNAL_SERVER_ERROR, corruptResponse)
      val expected = ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse))
      val actual = call
      actual shouldBe Left(expected)
    }
  }

}
