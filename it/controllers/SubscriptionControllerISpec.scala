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

package controllers

import helpers.ComponentSpecBase
import helpers.servicemocks.{AuthStub, BusinessDetailsStub}
import models.frontend.FESuccessResponse
import play.api.http.Status
import helpers.IntegrationTestConstants._

class SubscriptionControllerISpec extends ComponentSpecBase {
  "subscribe" should {
    "call the subscription service successfully when auth succeeds" in {
      AuthStub.stubGetAuthoritySuccess()

      AuthStub.stubGetIDsSuccess()

      BusinessDetailsStub.stubGetBusinessDetailsSuccess(testNino)

      val res = IncomeTaxSubscription.createSubscription(testNino)

      BusinessDetailsStub.verifyGetBusinessDetails(testNino)

      res.status shouldBe Status.OK
      res.json.as[FESuccessResponse] shouldBe FESuccessResponse(Some(testMtditId))
    }
  }
}
