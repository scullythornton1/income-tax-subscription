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

import play.api.http.Status.{INTERNAL_SERVER_ERROR, _}
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.services.mocks.MockEnrolmentService
import utils.TestConstants._
import utils.TestConstants.GG._
import utils.TestConstants.GG.KnownFactsResponse._
import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentServiceSpec extends MockEnrolmentService {

  implicit val hc = HeaderCarrier()

  "EnrolmentService.createEnrolment" should {

    val dummyResponse = Json.parse("{}")

    def call = await(TestEnrolmentService.createEnrolment(testNino, testMtditId))

    "return OK response correctly when both KnownFactsAdd and ggEnrol are successful" in {
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((OK, dummyResponse))
      call.right.get.status shouldBe OK
    }

    "return BAD request response correctly when KnownFactsAdd successful and ggEnrol fail" in {
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((BAD_REQUEST, dummyResponse))
      call.right.get.status shouldBe BAD_REQUEST
    }

    "return BAD_REQUEST response correctly" in {
      mockAddKnownFacts(knowFactsRequest)(GATEWAY_ERROR)
      call.left.get.status shouldBe INTERNAL_SERVER_ERROR
    }

  }

  "EnrolmentService.addKnownFacts" should {

    def call = await(TestEnrolmentService.addKnownFacts(testNino, testMtditId))

    "return the safeId when the registration is successful" in {
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      call.right.get.linesUpdated shouldBe 1
    }

    "return the error if registration fails" in {
      mockAddKnownFacts(knowFactsRequest)(SERVICE_DOES_NOT_EXISTS)
      call.left.get.status shouldBe BAD_REQUEST
    }

    "return the error when both registration is conflict but lookup is unsuccessful" in {
      mockAddKnownFacts(knowFactsRequest)(GATEWAY_ERROR)
      call.left.get.status shouldBe INTERNAL_SERVER_ERROR
    }

  }

  "EnrolmentService.ggEnrol" should {

    val dummyResponse = Json.parse("{}")

    def call = await(TestEnrolmentService.ggEnrol(testNino, testMtditId))

    "return OK response correctly" in {
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)(OK, dummyResponse)
      call.status shouldBe OK
    }

    "return BAD_REQUEST response correctly" in {
      mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)(BAD_REQUEST, dummyResponse)
      call.status shouldBe BAD_REQUEST
    }

  }

}
