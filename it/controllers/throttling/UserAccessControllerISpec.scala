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
import helpers.DatabaseHelpers._
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub
import play.api.http.Status._

class UserAccessControllerISpec extends ComponentSpecBase {
  "GET /throttle/:nino" should {
    "return OK when the service has not received any requests" in {
      Given("The database is empty")
      IncomeTaxSubscription.dropThrottleRepo()

      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      When("I call the endpoint")
      val res = IncomeTaxSubscription.checkUserAccess(testNino)

      Then("The result should have a HTTP status of OK")
      res should have(
        httpStatus(OK)
      )
    }

    "return OK when the service has not received too many requests" in {
      Given("The database contains a non full user count record")
      IncomeTaxSubscription.insertUserCount(nonFullUserCount)

      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      When("I call the endpoint")
      val res = IncomeTaxSubscription.checkUserAccess(testNino)

      Then("The result should have a HTTP status of OK")
      res should have(
        httpStatus(OK)
      )
    }

    "return OK for a returning user" in {
      Given("The database contains a full user count record which contains the matching user")
      IncomeTaxSubscription.insertUserCount(matchingUserCount)

      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      When("I call the endpoint")
      val res = IncomeTaxSubscription.checkUserAccess(testNino)

      Then("The result should have a HTTP status of OK")
      res should have(
        httpStatus(OK)
      )
    }

    "return TOO_MANY_REQUESTS when the service has received too many requests and it is a new userID" in {
      Given("The database contains a full user count record which contains the matching user")
      IncomeTaxSubscription.insertUserCount(maxUserCount)

      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      When("I call the endpoint")
      val res = IncomeTaxSubscription.checkUserAccess(testNino)

      Then("The result should have a HTTP status of TOO_MANY_REQUESTS")
      res should have(
        httpStatus(TOO_MANY_REQUESTS)
      )
    }

    "return FORBIDDEN when auth fails" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthFailure()

      When("I call the endpoint")
      val res = IncomeTaxSubscription.checkUserAccess(testNino)

      Then("The result should have a HTTP status of FORBIDDEN")
      res should have(
        httpStatus(FORBIDDEN)
      )
    }
  }
}
