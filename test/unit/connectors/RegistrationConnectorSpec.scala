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
import models.registration._
import org.scalatestplus.play.OneAppPerSuite
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}
import uk.gov.hmrc.play.test.UnitSpec
import unit.connectors.mocks.MockHttp
import utils.JsonUtil._
import utils.TestConstants

class RegistrationConnectorSpec extends UnitSpec with MockHttp with OneAppPerSuite {

  lazy val config: Configuration = app.injector.instanceOf[Configuration]
  lazy val logging: Logging = app.injector.instanceOf[Logging]
  lazy val http: HttpPost = mockHttpPost

  object TestRegistrationConnector extends RegistrationConnector(config, logging, http)

  implicit val hc = HeaderCarrier()

  val individual = IndividualModel("f", "l")
  val register = RegistrationRequestModel(isAnAgent = false, individual = individual)
  val nino: String = TestConstants.testNino

  "RegistrationConnector.register" should {
    "Put in the correct headers" in {
      val rHc = TestRegistrationConnector.createHeaderCarrier(hc)
      rHc.headers.contains("Authorization" -> s"Bearer ${config.getString("microservice.services.registration.authorization-token").get}") shouldBe true
      rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
      rHc.headers.contains("Environment" -> config.getString("microservice.services.registration.environment").get) shouldBe true
    }

    "Post to the correct url" in {

    }

    val safeId = "XE0001234567890"
    import TestConstants.RegistrationResponse._

    def call = await(TestRegistrationConnector.register(nino, register))

    "parse and return the success response correctly" in {
      setupMockHttpPost()(OK, successResponse(safeId))
      val expected = RegistrationSuccessResponseModel(safeId)
      val actual = call
      actual shouldBe Right(expected)
    }

    "parse and return the Bad request response correctly" in {
      val reason = "Your submission contains one or more errors. Failed Parameter(s) - [idType, idNumber, payload]"
      setupMockHttpPost()(BAD_REQUEST, failureResponse(reason))
      val expected = RegistrationFailureResponseModel(reason)
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Resource not found response correctly" in {
      val reason = "Resource not found"
      setupMockHttpPost()(NOT_FOUND, failureResponse(reason))
      val expected = RegistrationFailureResponseModel(reason)
      val actual = call
      actual shouldBe Left(expected)
    }

    "parse and return the Server error response correctly" in {
      val reason = "Server Error"
      setupMockHttpPost()(INTERNAL_SERVER_ERROR, failureResponse(reason))
      val expected = RegistrationFailureResponseModel(reason)
      val actual = call
      actual shouldBe Left(expected)

    }

    "parse and return the Service unavailable response correctly" in {
      val reason = "Service unavailable"
      setupMockHttpPost()(SERVICE_UNAVAILABLE, failureResponse(reason))
      val expected = RegistrationFailureResponseModel(reason)
      val actual = call
      actual shouldBe Left(expected)

    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      setupMockHttpPost()(INTERNAL_SERVER_ERROR, corruptResponse)
      val expected = RegistrationResponse.parseFailure(corruptResponse)
      val actual = call
      actual shouldBe Left(expected)
    }
  }

}
