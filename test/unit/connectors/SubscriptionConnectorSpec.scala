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

import models.{ErrorModel, IncomeSourcesModel, PropertySubscriptionResponseModel}
import models.subscription.business._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.connectors.mocks.MockSubscriptionConnector
import utils.JsonUtils._
import utils.TestConstants._
import BusinessSubscriptionResponse._
import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionConnectorSpec extends MockSubscriptionConnector {

  implicit val hc = HeaderCarrier()

  val businessPayload = BusinessSubscriptionRequestModel(
    List(BusinessDetailsModel(
      accountingPeriodStartDate = "2017-05-01",
      accountingPeriodEndDate = "2018-04-30",
      tradingName = "Test Business",
      cashOrAccruals = "cash"
    ))
  )

  "The connector should have the correct POST request headers for DES" in {
    val rHc = TestSubsscriptionConnector.createHeaderCarrierPost(hc)
    rHc.headers.contains("Authorization" -> s"Bearer ${appConfig.desToken}") shouldBe true
    rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
    rHc.headers.contains("Environment" -> appConfig.desEnvironment) shouldBe true
  }

  "SubscriptionConnector.businessSubscribe" should {

    def setupMockBusinessSubscribe(status: Int, response: JsValue): Unit =
      super.setupMockBusinessSubscribe(testNino, businessPayload)(status, response)

    def businessCall = await(TestSubsscriptionConnector.businessSubscribe(testNino, businessPayload))

    "Post to the correct url" in {
      TestSubsscriptionConnector.businessSubscribeUrl(testNino) should endWith(s"/income-tax-self-assessment/nino/$testNino/business")
    }

    "parse and return the success response correctly" in {
      setupMockBusinessSubscribe(OK, successResponse(testSafeId, testMtditId, testSourceId))
      val expected = Right(BusinessSubscriptionSuccessResponseModel(testSafeId, testMtditId, List(IncomeSourceModel(testSourceId))))
      val actual = businessCall
      actual shouldBe expected
    }

    "parse and return the Bad request response correctly" in {
      val code = "INVALID_NINO"
      setupMockBusinessSubscribe(BAD_REQUEST, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(BAD_REQUEST, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = businessCall
      actual shouldBe expected
    }

    "parse and return the Resource not found response correctly" in {
      val code = "NOT_FOUND"
      setupMockBusinessSubscribe(NOT_FOUND, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(NOT_FOUND, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = businessCall
      actual shouldBe expected
    }

    "parse and return the Conflict error response correctly" in {
      val code = "CONFLICT"
      setupMockBusinessSubscribe(CONFLICT, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(CONFLICT, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = businessCall
      actual shouldBe expected
    }

    "parse and return the Server error response correctly" in {
      val code = "SERVER_ERROR"
      setupMockBusinessSubscribe(INTERNAL_SERVER_ERROR, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(INTERNAL_SERVER_ERROR, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = businessCall
      actual shouldBe expected
    }

    "parse and return the Service unavailable response correctly" in {
      val code = "SERVICE_UNAVAILABLE"
      setupMockBusinessSubscribe(SERVICE_UNAVAILABLE, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(SERVICE_UNAVAILABLE, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = businessCall
      actual shouldBe expected
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      setupMockBusinessSubscribe(INTERNAL_SERVER_ERROR, corruptResponse)
      val expected = Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse)))
      val actual = businessCall
      actual shouldBe expected
    }
  }

  "SubscriptionConnector.propertySubscribe" should {

    def setupMockPropertySubscribe(status: Int, response: JsValue): Unit =
      super.setupMockPropertySubscribe(testNino)(status, response)

    def propertyCall = await(TestSubsscriptionConnector.propertySubscribe(testNino))

    lazy val jsSuccess = Json.parse(propertySubscriptionSuccessResponse)

    "Post to the correct url" in {
      TestSubsscriptionConnector.propertySubscribeUrl(testNino) should endWith(s"/income-tax-self-assessment/nino/$testNino/properties")
    }

    "parse and return success response" in {

      setupMockPropertySubscribe(OK, jsSuccess)
      val expected = PropertySubscriptionResponseModel(
        safeId = "XA0001234567890", mtditId = "mdtitId001", incomeSource = IncomeSourcesModel(incomeSourceId = "sourceId0001")
      )
      val actual = propertyCall
      actual shouldBe Right(expected)
    }

    "parse and return Invalid Payload response" in {
      val reason = "Submission has not passed validation. Invalid PAYLOAD."
      val code = "INVALID_PAYLOAD"
      val jsFailure = Json.parse(propertySubscriptionFailureResponse(code, reason))
      setupMockPropertySubscribe(BAD_REQUEST, jsFailure)
      val expected = ErrorModel(BAD_REQUEST, code, reason)
      propertyCall shouldBe Left(expected)
    }

    "parse and return Invalid Nino response" in {
      val reason = "Submission has not passed validation. Invalid parameter NINO."
      val code = "INVALID_NINO"
      val jsFailure = Json.parse(propertySubscriptionFailureResponse(code, reason))
      setupMockPropertySubscribe(BAD_REQUEST, jsFailure)
      val expected = ErrorModel(BAD_REQUEST, code, reason)
      propertyCall shouldBe Left(expected)
    }

    "parse and return not found response" in {
      val reason = "The remote endpoint has indicated that no data can be found."
      val code = "NOT_FOUND_NINO"
      val jsFailure = Json.parse(propertySubscriptionFailureResponse(code, reason))
      setupMockPropertySubscribe(NOT_FOUND, jsFailure)
      val expected = ErrorModel(NOT_FOUND, code, reason)
      propertyCall shouldBe Left(expected)
    }

    "parse and return server error response" in {
      val reason = "DES is currently experiencing problems that require live service intervention."
      val code = "SERVER_ERROR"
      val jsFailure = Json.parse(propertySubscriptionFailureResponse(code, reason))
      setupMockPropertySubscribe(INTERNAL_SERVER_ERROR, jsFailure)
      val expected = ErrorModel(INTERNAL_SERVER_ERROR, code, reason)
      propertyCall shouldBe Left(expected)
    }

    "parse and return service unavailable response" in {
      val reason = "Dependent systems are currently not responding."
      val code = "SERVICE_UNAVAILABLE"
      val jsFailure = Json.parse(propertySubscriptionFailureResponse(code, reason))
      setupMockPropertySubscribe(SERVICE_UNAVAILABLE, jsFailure)
      val expected = ErrorModel(SERVICE_UNAVAILABLE, code, reason)
      propertyCall shouldBe Left(expected)
    }
  }
}
