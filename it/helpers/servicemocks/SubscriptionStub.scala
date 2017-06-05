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

package helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import connectors.SubscriptionConnector
import models.subscription.IncomeSourceModel
import models.subscription.business.BusinessSubscriptionSuccessResponseModel
import helpers.IntegrationTestConstants._
import models.subscription.property.PropertySubscriptionResponseModel
import play.api.http.Status._

object SubscriptionStub extends WireMockMethods {
  val testBusinessSubscriptionResponse: BusinessSubscriptionSuccessResponseModel =
    BusinessSubscriptionSuccessResponseModel(
      testSafeId,
      testMtditId,
      List(IncomeSourceModel(testSourceId))
    )

  val testPropertySubscriptionResponse: PropertySubscriptionResponseModel =
    PropertySubscriptionResponseModel(
      testSafeId,
      testMtditId,
      IncomeSourceModel(testSourceId)
    )

  def stubBusinessSubscribeSuccess(): StubMapping =
    when(method = POST, uri = SubscriptionConnector.businessSubscribeUri(testNino), body = businessSubscriptionRequestPayload)
    .thenReturn(status = OK, body = testBusinessSubscriptionResponse)
}
