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
import connectors.RegistrationConnector
import helpers.IntegrationTestConstants._
import models.registration.RegistrationSuccessResponseModel
import play.api.http.Status._

object RegistrationStub extends WireMockMethods {
  val successfulRegistrationResponse: RegistrationSuccessResponseModel = RegistrationSuccessResponseModel(testMtditId)

  def stubNewRegistrationSuccess(): StubMapping =
    when(method = POST, uri = RegistrationConnector.newRegistrationUri(testNino), body = registerRequestPayload)
      .thenReturn(status = OK, body = successfulRegistrationResponse)
}
