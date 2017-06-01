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

package controllers.subscription

import connectors.BusinessDetailsConnector._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.WireMockDSL.HTTPVerbMapping.Get
import helpers.WireMockDSL._
import helpers.servicemocks.AuthStub._
import helpers.servicemocks.BusinessDetailsStub._
import models.frontend.FESuccessResponse
import play.api.http.Status._

class SubscriptionStatusControllerISpec extends ComponentSpecBase {
  "subscribe" should {
    "call the subscription service successfully when auth succeeds" in {
      stub when Get(authority) thenReturn stubbedAuthResponse
      stub when Get(authIDs) thenReturn stubbedIDs
      stub when Get(getBusinessDetailsUri(testNino)) thenReturn registrationResponse

      IncomeTaxSubscription.getSubscriptionStatus(testNino) should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      stub verify Get(getBusinessDetailsUri(testNino))
    }

    "fail when get authority fails" in {
      stubGetAuthorityFailure()

      IncomeTaxSubscription.createSubscription(testNino) should have(
        httpStatus(UNAUTHORIZED),
        emptyBody
      )
    }
  }
}
