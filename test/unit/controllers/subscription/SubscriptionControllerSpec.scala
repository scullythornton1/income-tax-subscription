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

package unit.controllers.subscription

import controllers.subscription.SubscriptionController
import models.frontend.FESuccessResponse
import play.api.http.Status._
import play.api.mvc.{AnyContentAsJson, Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import unit.connectors.mocks.MockAuthConnector
import unit.services.mocks.MockSubscriptionManagerService
import utils.JsonUtils._
import utils.MaterializerSupport
import utils.TestConstants.AuthenticatorResponse._
import utils.TestConstants.GG.EnrolResponseExamples._
import utils.TestConstants.GG.KnownFactsResponse._
import utils.TestConstants.GG._
import utils.TestConstants._

import scala.concurrent.Future

class SubscriptionControllerSpec extends UnitSpec with MockSubscriptionManagerService with MaterializerSupport with MockAuthConnector {

  object TestController extends SubscriptionController(logging, TestSubscriptionManagerService, mockAuthConnector)

  def call(request: Request[AnyContentAsJson]): Future[Result] = TestController.subscribe(testNino)(request)

  "SubscriptionController" should {
    "return the id when successful, call enrol user if it is set to true" in {
      val fakeRequest: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(fePropertyRequest.copy(enrolUser = true))
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((OK, enrolSuccess))
      mockRefreshProfile(refreshSuccess)
      val result = call(fakeRequest)
      jsonBodyOf(result).as[FESuccessResponse].mtditId.get shouldBe testMtditId

      verifyMockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)(1)
      verifyRefreshProfile(1)
    }

    "return the id when successful, do not call enrol user if it is set to false" in {
      val fakeRequest: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(fePropertyRequest.copy(enrolUser = false))
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      val result = call(fakeRequest)
      jsonBodyOf(result).as[FESuccessResponse].mtditId shouldBe testMtditId

      verifyMockGovernmentGatewayEnrol()(0)
      verifyRefreshProfile(0)
    }

    "return failure when it's unsuccessful" in {
      val fakeRequest: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody("{}")
      val result = call(fakeRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }

}
