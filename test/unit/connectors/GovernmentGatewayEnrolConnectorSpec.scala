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

package unit.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.connectors.mocks.MockGovernmentGatewayEnrolConnector
import utils.TestConstants._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.http.Status._

class GovernmentGatewayEnrolConnectorSpec extends MockGovernmentGatewayEnrolConnector {

  implicit val hc = HeaderCarrier()
  val dummyResponse = Json.parse("{}")

  def result = TestGovernmentGatewayEnrolConnector.enrol(governmentGatewayEnrolPayload)

  "return OK response correctly" in {
    mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((OK, dummyResponse))
    val r = await(result)
    r.status shouldBe OK
  }

  "return BAD_REQUEST response correctly" in {
    mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((BAD_REQUEST, dummyResponse))
    val r = await(result)
    r.status shouldBe BAD_REQUEST
  }

  "return FORBIDDEN response correctly" in {
    mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((FORBIDDEN, dummyResponse))
    val r = await(result)
    r.status shouldBe FORBIDDEN
  }
  "return INTERNAL_SERVER_ERROR response correctly" in {
    mockGovernmentGatewayEnrol(governmentGatewayEnrolPayload)((INTERNAL_SERVER_ERROR, dummyResponse))
    val r = await(result)
    r.status shouldBe INTERNAL_SERVER_ERROR
  }
}
