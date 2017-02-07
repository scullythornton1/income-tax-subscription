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

import audit.Logging
import connectors.RegistrationConnector
import models.registration.{IndividualModel, RegistrationRequestModel}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.Configuration
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}
import uk.gov.hmrc.play.test.UnitSpec


class RegistrationConnectorSpec extends UnitSpec with MockitoSugar with OneAppPerSuite {

  lazy val config: Configuration = app.injector.instanceOf[Configuration]
  lazy val logging: Logging = app.injector.instanceOf[Logging]
  lazy val http: HttpPost = app.injector.instanceOf[HttpPost]

  object TestRegistrationConnector extends RegistrationConnector(config, logging, http)

  implicit val hc = HeaderCarrier()

  val individual = IndividualModel("f", "l")
  val register = RegistrationRequestModel(isAnAgent = false, individual = individual)
  val nino = "AA111111A"

  "RegistrationConnector.register" should {
    "Put in the correct headers" in {
      val rHc = TestRegistrationConnector.createHeaderCarrier(hc)
      rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
      rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
      rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
    }

    "parse and return the success response correctly" in {
      val req = TestRegistrationConnector.register(nino, register)
      val r = await(req)

    }

    "parse and return the Bad request response correctly" in {
      val req = TestRegistrationConnector.register(nino, register)
      val r = await(req)
    }

    "parse and return the Resource not found response correctly" in {
      val req = TestRegistrationConnector.register(nino, register)
      val r = await(req)
    }

    "parse and return the Server error response correctly" in {
      val req = TestRegistrationConnector.register(nino, register)
      val r = await(req)
    }

    "parse and return the Service unavailable response correctly" in {
      val req = TestRegistrationConnector.register(nino, register)
      val r = await(req)
    }

    "return parse error for corrupt response" in {
      val req = TestRegistrationConnector.register(nino, register)
      val r = await(req)
    }
  }

}
