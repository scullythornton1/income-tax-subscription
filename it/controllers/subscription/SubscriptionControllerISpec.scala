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

import connectors._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.WireMockDSL.HTTPVerbMapping.{Get, Post}
import helpers.WireMockDSL._
import helpers.servicemocks.AuthStub._
import helpers.servicemocks.BusinessDetailsStub._
import helpers.servicemocks.GGAdminStub._
import helpers.servicemocks.SubscriptionStub._
import models.frontend.FESuccessResponse
import play.api.http.Status._
import play.api.libs.json.Json

class SubscriptionControllerISpec extends ComponentSpecBase {
  "subscribe" should {
    "call the subscription service successfully when auth succeeds for a business registration" in {
      stub when Get(authority) thenReturn stubbedAuthResponse
      stub when Get(authIDs) thenReturn stubbedIDs
      multiline(
        stub
          when Post(RegistrationConnector.newRegistrationUri(testNino), registerRequestPayload)
          thenReturn registrationResponse
      )
      multiline(
        stub
          when Post(SubscriptionConnector.businessSubscribeUri(testNino), businessSubscriptionRequestPayload)
          thenReturn testBusinessSubscriptionResponse
      )
      stub when Post(GGAdminConnector.addKnownFactsUri) thenReturn testAddKnownFactsResponse
      stub when Post(GGConnector.enrolUri) thenReturn OK

      stub when Post(AuthenticatorConnector.refreshProfileUri) thenReturn NO_CONTENT

      IncomeTaxSubscription.createSubscription(feBusinessRequest) should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )
    }

    "call the subscription service successfully when auth succeeds for a property registration" in {
      stub when Get(authority) thenReturn stubbedAuthResponse
      stub when Get(authIDs) thenReturn stubbedIDs
      multiline(
        stub
          when Post(RegistrationConnector.newRegistrationUri(testNino), registerRequestPayload)
          thenReturn registrationResponse
      )
      multiline(
        stub
          when Post(SubscriptionConnector.propertySubscribeUri(testNino), Json.obj())
          thenReturn testPropertySubscriptionResponse
      )
      stub when Post(GGAdminConnector.addKnownFactsUri) thenReturn testAddKnownFactsResponse
      stub when Post(GGConnector.enrolUri) thenReturn OK
      stub when Post(AuthenticatorConnector.refreshProfileUri) thenReturn NO_CONTENT

      IncomeTaxSubscription.createSubscription(fePropertyRequest) should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )
    }

    "call the subscription service successfully when auth succeeds for a business and property registration" in {
      stub when Get(authority) thenReturn stubbedAuthResponse
      stub when Get(authIDs) thenReturn stubbedIDs
      multiline(
        stub
          when Post(RegistrationConnector.newRegistrationUri(testNino), registerRequestPayload)
          thenReturn registrationResponse
      )
      multiline(
        stub
          when Post(SubscriptionConnector.propertySubscribeUri(testNino), Json.obj())
          thenReturn testPropertySubscriptionResponse
      )
      multiline(
        stub
          when Post(SubscriptionConnector.businessSubscribeUri(testNino), businessSubscriptionRequestPayload)
          thenReturn testBusinessSubscriptionResponse
      )
      stub when Post(GGAdminConnector.addKnownFactsUri) thenReturn testAddKnownFactsResponse
      stub when Post(GGConnector.enrolUri) thenReturn OK
      stub when Post(AuthenticatorConnector.refreshProfileUri) thenReturn NO_CONTENT

      IncomeTaxSubscription.createSubscription(feBothRequest) should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      stub verify Post(SubscriptionConnector.businessSubscribeUri(testNino), businessSubscriptionRequestPayload)
      stub verify Post(SubscriptionConnector.propertySubscribeUri(testNino), Json.obj())
    }

    "fail when get authority fails" in {
      stubGetAuthorityFailure()

      IncomeTaxSubscription.createSubscription(feBusinessRequest) should have(
        httpStatus(UNAUTHORIZED),
        emptyBody
      )
    }
  }
}
