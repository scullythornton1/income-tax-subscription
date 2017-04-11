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

import controllers.subscription.SubscriptionStatusController
import models.frontend.FESuccessResponse
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import unit.services.mocks.MockSubscriptionStatusService
import utils.MaterializerSupport
import utils.TestConstants._

import scala.concurrent.Future

class SubscriptionStatusControllerSpec extends UnitSpec with MockSubscriptionStatusService with MaterializerSupport {

  object TestController extends SubscriptionStatusController(logging, TestSubscriptionStatusService)

  def call: Future[Result] = TestController.checkSubscriptionStatus(testNino)(FakeRequest())

  "SubscriptionStatusController" should {
    "when the queried person has no prior mtditsa enrolment return OK with an empty body" in {
      mockBusinessDetails(getBusinessDetailsNotFound)
      val result = call
      status(result) shouldBe OK
      jsonBodyOf(result).as[FESuccessResponse].mtditId shouldBe None
    }

    "when the queried person has a prior mtditsa enrolment return OK with the id" in {
      mockBusinessDetails(getBusinessDetailsSuccess)
      val result = call
      status(result) shouldBe OK
      jsonBodyOf(result).as[FESuccessResponse].mtditId shouldBe Some(testMtditId)
    }
  }

}
