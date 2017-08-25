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

package services

import services.mocks.TestLockoutStatusService
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.TestConstants.testArn
import play.api.http.Status._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import utils.TestConstants._

import scala.concurrent.Future

class LockoutStatusServiceSpec extends UnitSpec with TestLockoutStatusService {

  implicit val hc = HeaderCarrier()

  def call = await(TestLockoutStatusService.checkLockoutStatus(testArn))

  "LockoutStatusService" should {

    "return a testLockoutSuccess if they are locked out" in {
      mockGetLockoutStatus(testArn)(Future.successful(Some(testLockoutResponse)))
      val result = await(call)
      result shouldBe testLockoutSuccess

    }

    "return a testLockoutNone if they are not locked out" in {
      mockNotLockedOut(testArn)
      val result = await(call)
      result shouldBe testLockoutNone

    }

    "return a testLockoutFailure if it fails" in {
      mockLockedOutFailure(testArn)
      val result = await(call)
      result shouldBe testLockoutFailure

    }
  }

}
