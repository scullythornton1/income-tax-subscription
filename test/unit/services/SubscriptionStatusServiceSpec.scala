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

import models.frontend.FESuccessResponse
import play.api.http.Status._
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.services.mocks.MockSubscriptionStatusService
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import utils.Implicits._

class SubscriptionStatusServiceSpec extends MockSubscriptionStatusService {

  implicit val hc = HeaderCarrier()

  "SubscriptionStatusService.checkMtditsaSubscroption" should {

    def call = await(TestSubscriptionStatusService.checkMtditsaSubscription(testNino))

    "return the Right(NONE) when the person does not have a mtditsa subscription" in {
      mockBusinessDetails(getBusinessDetailsNotFound)
      call.right.get shouldBe None
    }

    "return the Right(Some(FESuccessResponse)) when the person already have a mtditsa subscription" in {
      mockBusinessDetails(getBusinessDetailsSuccess)
      // testMtditId must be the same value defined in getBusinessDetailsSuccess
      call.right.get shouldBe Some(FESuccessResponse(testMtditId))
    }

    "return the error for other error type" in {
      mockBusinessDetails(getBusinessDetailsServerError)
      call.left.get.status shouldBe INTERNAL_SERVER_ERROR
    }

  }

}
