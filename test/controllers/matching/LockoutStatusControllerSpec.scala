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

package controllers.matching

import play.api.http.Status._
import play.api.mvc.Result
import play.api.test.FakeRequest
import services.mocks.{MockAuthService, MockLockoutStatusService}
import uk.gov.hmrc.play.test.UnitSpec
import utils.MaterializerSupport
import utils.TestConstants.testArn

import scala.concurrent.Future


class LockoutStatusControllerSpec extends UnitSpec with MockLockoutStatusService with MaterializerSupport with MockAuthService {

  object TestController extends LockoutStatusController(mockAuthService, mockLockoutStatusService)

  def call: Future[Result] = TestController.checkLockoutStatus(testArn)(FakeRequest())

  "LockoutStatusController" should {
    "when the queried ARN is locked out should return OK" in {
      mockAuthSuccess()
      mockLockedOut(testArn)

      val result = call
      status(result) shouldBe OK
    }

    "when the queried ARN is not locked out should return a NOT_FOUND" in {
      mockAuthSuccess()
      mockNotLockedOut(testArn)

      val result = call
      status(result) shouldBe NOT_FOUND
    }

    "when anything else is returned should return an INTERNAL_SERVER_ERROR" in {
      mockAuthSuccess()
      mockLockedOutFailure(testArn)

      val result = call
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }


  }

}