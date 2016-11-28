/*
 * Copyright 2016 HM Revenue & Customs
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

package it

import com.github.tomakehurst.wiremock.client.WireMock._
import controllers.Documentation
import it.utils.{MicroserviceLocalRunSugar, WiremockServiceLocatorSugar}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec


/**
  * Testcase to verify the capability of integration with the API platform.
  *
  * 1, To integrate with API platform the service needs to register itself to the service locator by calling the /registration endpoint and providing
  * - application name
  * - application url
  *
  * See: https://confluence.tools.tax.service.gov.uk/display/ApiPlatform/API+Platform+Architecture+with+Flows
  */
class PlatformIntegrationSpec extends UnitSpec with MockitoSugar with ScalaFutures with WiremockServiceLocatorSugar with BeforeAndAfter {

   before {
     startMockServer()
     stubRegisterEndpoint(204)
   }

   after {
     stopMockServer()
   }

   trait Setup {
     val documentationController = new Documentation {}
     val request = FakeRequest()
   }

   "microservice" should {

     "register itelf to service-locator" in new MicroserviceLocalRunSugar with Setup {
       override val additionalConfiguration: Map[String, Any] = Map(
         "appName" -> "application-name",
         "appUrl" -> "http://microservice-name.service",
         "microservice.services.service-locator.host" -> stubHost,
         "microservice.services.service-locator.port" -> stubPort
       )
       run {
         () => {
           verify(1,postRequestedFor(urlMatching("/registration")).
             withHeader("content-type", equalTo("application/json")).
             withRequestBody(equalTo(regPayloadStringFor("application-name", "http://microservice-name.service"))))
         }
       }
     }
   }
 }
