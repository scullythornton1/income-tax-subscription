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

package controllers.throttling

import helpers.ComponentSpecBase
import helpers.WireMockDSL.HTTPVerbMapping.Get
import helpers.WireMockDSL._
import helpers.servicemocks.AuthStub._
import helpers.IntegrationTestConstants._
import play.api.http.Status._
import helpers.DatabaseHelpers._
import play.api.libs.concurrent.Execution.Implicits._

class UserAccessControllerISpec extends ComponentSpecBase {
  "GET /throttle/:nino" should {
    "return OK when the service has not received any requests" in {
      stub when Get(authority) thenReturn successfulAuthResponse
      stub when Get(authIDs) thenReturn userIDs

      IncomeTaxSubscription.checkUserAccess(testNino) should have (
        httpStatus(OK)
      )
    }

    "return OK when the service has not received too many requests" in {
      IncomeTaxSubscription.insertUserCount(nonFullUserCount)

      stub when Get(authority) thenReturn successfulAuthResponse
      stub when Get(authIDs) thenReturn userIDs

      IncomeTaxSubscription.checkUserAccess(testNino) should have (
        httpStatus(OK)
      )
    }

    "return OK for a returning user" in {
      IncomeTaxSubscription.insertUserCount(matchingUserCount)

      stub when Get(authority) thenReturn successfulAuthResponse
      stub when Get(authIDs) thenReturn userIDs

      IncomeTaxSubscription.checkUserAccess(testNino) should have (
        httpStatus(OK)
      )
    }

    "return TOO_MANY_REQUESTS when the service has received too many requests and it is a new userID" in {
      IncomeTaxSubscription.insertUserCount(maxUserCount)

      stub when Get(authority) thenReturn successfulAuthResponse
      stub when Get(authIDs) thenReturn userIDs

      IncomeTaxSubscription.checkUserAccess(testNino) should have (
        httpStatus(TOO_MANY_REQUESTS)
      )
    }
  }
}
