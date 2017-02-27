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

import models.frontend.FERequest
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.services.mocks.MockSubscriptionManagerService
import utils.TestConstants.GG.KnownFactsResponse._
import utils.TestConstants.GG.EnrolResponseExamples._
import utils.TestConstants.GG._
import utils.TestConstants._
import utils.TestConstants.AuthenticatorResponse._

import scala.concurrent.ExecutionContext

class RosmAndEnrolManagerServiceSpec extends MockSubscriptionManagerService {

  implicit val hc = HeaderCarrier()
  implicit val ec = ExecutionContext.Implicits.global

  "The RosmAndEnrolManagerService.rosmAndEnrol action" should {

    def call(request: FERequest) = await(TestSubscriptionManagerService.rosmAndEnrol(request))

    "return the mtditId when reg, subscribe, known facts, enrol and refresh is successful (property only)" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((OK, enrolSuccess))
      mockRefreshProfile(refreshSuccess)
      call(fePropertyRequest).right.get.mtditId shouldBe testMtditId
    }

    "return the mtditId when reg, subscribe, known facts, enrol and refresh is successful (business only)" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((OK, enrolSuccess))
      mockRefreshProfile(refreshSuccess)
      call(feBusinessRequest).right.get.mtditId shouldBe testMtditId
    }

    "return the mtditId when reg, subscribe, known facts, enrol and refresh is successful (both business and property)" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((OK, enrolSuccess))
      mockRefreshProfile(refreshSuccess)
      call(feBothRequest).right.get.mtditId shouldBe testMtditId
    }

    "return an error when reg, subscribe, known facts, enrol is successful but refresh fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((OK, enrolSuccess))
      mockRefreshProfile(refreshFailure)
      call(feBothRequest).left.get.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return an error when reg, subscribe, known facts is successful but enrol fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((BAD_REQUEST, enrolFailure))
      call(feBothRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return an error when reg, subscribe but known facts fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      mockAddKnownFacts(knowFactsRequest)(SERVICE_DOES_NOT_EXISTS)
      call(feBothRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return an error when reg, property subscribe success, business subscribe fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(NOT_FOUND_NINO)
      call(feBothRequest).left.get.status shouldBe NOT_FOUND
    }

    "return an error when reg, property subscribe fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(NOT_FOUND_NINO)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(NOT_FOUND_NINO)
      call(feBothRequest).left.get.status shouldBe NOT_FOUND
    }

    "return an error when reg fails" in {
      mockRegister(registerRequestPayload)(UNAVAILABLE)
      call(feBothRequest).left.get.status shouldBe SERVICE_UNAVAILABLE
    }
  }

  "The RosmAndEnrolManagerService.orchestrateROSM action" should {

    def call(request: FERequest) = await(TestSubscriptionManagerService.orchestrateROSM(request))

    "return the mtditID when registration and subscription for property is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      call(fePropertyRequest).right.get.mtditId shouldBe testMtditId
    }

    "return the mtditID when registration and subscription for business is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      call(feBusinessRequest).right.get.mtditId shouldBe testMtditId
    }

    "return the mtditID when registration and subscription for both Property and Business is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      call(feBothRequest).right.get.mtditId shouldBe testMtditId
    }

    "return the error if registration fails" in {
      mockRegister(registerRequestPayload)(INVALID_NINO)
      call(fePropertyRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration successful but property subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(INVALID_NINO)
      call(fePropertyRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration successful but business subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(INVALID_NINO)
      call(feBusinessRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration and property successful, but business subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(INVALID_NINO)
      call(feBothRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration and business successful, but property subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(INVALID_NINO)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      call(feBothRequest).left.get.status shouldBe BAD_REQUEST
    }

  }

  "RosmAndEnrolManagerService.orchestrateEnrolment" should {

    val dummyResponse = Json.parse("{}")

    def call = await(TestSubscriptionManagerService.orchestrateEnrolment(testNino, testMtditId))

    "return OK response correctly when both KnownFactsAdd and ggEnrol are successful" in {
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((OK, dummyResponse))
      call.right.get.status shouldBe OK
    }

    "return BAD request response correctly when KnownFactsAdd successful and ggEnrol fail" in {
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((BAD_REQUEST, dummyResponse))
      call.left.get.status shouldBe BAD_REQUEST
    }

    "return BAD_REQUEST response correctly" in {
      mockAddKnownFacts(knowFactsRequest)(GATEWAY_ERROR)
      call.left.get.status shouldBe INTERNAL_SERVER_ERROR
    }

  }

}
