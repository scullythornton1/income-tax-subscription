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
import connectors.GGAdminConnector
import models.gg._
import helpers.IntegrationTestConstants._
import play.api.http.Status._

object GGAdminStub extends WireMockMethods {
  val testAddKnownFactsResponse: KnownFactsSuccessResponseModel = KnownFactsSuccessResponseModel(1)
  val testAddKnownFactsFailureResponse: KnownFactsFailureResponseModel = KnownFactsFailureResponseModel(BAD_REQUEST, testErrorReason)

  def stubAddKnownFactsSuccess(): StubMapping =
    when(method = POST, uri = GGAdminConnector.addKnownFactsUri)
      .thenReturn(status = OK, body = testAddKnownFactsResponse)

  def stubAddKnownFactsFailure(): StubMapping =
    when(method = POST, uri = GGAdminConnector.addKnownFactsUri)
      .thenReturn(status = BAD_REQUEST, body = testAddKnownFactsFailureResponse)
}
