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

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks._
import models.frontend.FESuccessResponse
import play.api.http.Status._

class SubscriptionControllerISpec extends ComponentSpecBase {
  "subscribe" should {
    "call the subscription service successfully when auth succeeds for a business registration" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubBusinessSubscribeSuccess()
      GGAdminStub.stubAddKnownFactsSuccess()
      GGConnectorStub.stubEnrolSuccess()
      GGAuthenticationStub.stubRefreshProfileSuccess()

      When("I call POST /subscription/:nino where nino is the test nino with a Business Request")
      val res = IncomeTaxSubscription.createSubscription(feBusinessRequest)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()
    }
    "call the subscription service successfully when auth succeeds for a property registration" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubPropertySubscribeSuccess()
      GGAdminStub.stubAddKnownFactsSuccess()
      GGConnectorStub.stubEnrolSuccess()
      GGAuthenticationStub.stubRefreshProfileSuccess()

      When("I call POST /subscription/:nino where nino is the test nino with a Property Request")
      val res = IncomeTaxSubscription.createSubscription(fePropertyRequest)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()
    }

    "call the subscription service successfully when auth succeeds for a business and property registration" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubBusinessSubscribeSuccess()
      SubscriptionStub.stubPropertySubscribeSuccess()
      GGAdminStub.stubAddKnownFactsSuccess()
      GGConnectorStub.stubEnrolSuccess()
      GGAuthenticationStub.stubRefreshProfileSuccess()

      When("I call POST /subscription/:nino where nino is the test nino with both a property request and a business request")
      val res = IncomeTaxSubscription.createSubscription(feBothRequest)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      Then("Business subscription should have been called")
      SubscriptionStub.verifyBusinessSubscribe()

      Then("Property subscription should have been called")
      SubscriptionStub.verifyPropertySubscribe()

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()
    }
  }
}
