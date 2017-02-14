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

package unit.services

import models.frontend.FERequest
import play.api.http.Status._
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.services.mocks.MockSubscriptionManagerService
import utils.TestConstants._

import scala.concurrent.ExecutionContext

class SubscriptionManagerServiceSpec extends MockSubscriptionManagerService {

  implicit val hc = HeaderCarrier()
  implicit val ec = ExecutionContext.Implicits.global

  "The SubscriptionManagerService.orchestrateSubscription action" should {

    def call(request: FERequest) = await(TestSubscriptionManagerService.orchestrateSubscription(request))

    "return the mtditID when registration and subscription for property is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      call(fePropertyRequest).right.get.mtditId shouldBe testMtditId
    }

    "return the mtditID when registration and subscription for business is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      call(feBusinessRequest).right.get.mtditId shouldBe testMtditId
    }

    "return the mtditID when registration and subscription for both Property and Business is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      call(feBothRequest).right.get.mtditId shouldBe testMtditId
    }

    "return the error if registration fails" in {
      mockRegister(registerRequestPayload)(INVALID_NINO)
      call(fePropertyRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration successful but property subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(INVALID_NINO)
      call(fePropertyRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration successful but business subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(INVALID_NINO)
      call(feBusinessRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration and property successful, but business subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(INVALID_NINO)
      call(feBothRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration and business successful, but property subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(INVALID_NINO)
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      call(feBothRequest).left.get.status shouldBe BAD_REQUEST
    }

  }

}
