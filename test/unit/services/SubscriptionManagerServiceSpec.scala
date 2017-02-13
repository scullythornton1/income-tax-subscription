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

import play.api.http.Status._
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.services.mocks.MockSubscriptionManagerService

class SubscriptionManagerServiceSpec extends MockSubscriptionManagerService {

  implicit val hc = HeaderCarrier()

  def call = await(TestSubscriptionManagerService.orchestrateSubscription(request = feRequest))

  "SubscriptionManagerService" should {
    "return the safeId when the registration is successful" in {
      setupRegister(newRegSuccess)
      val response = call
      response.isRight shouldBe true
      response.right.get.safeId shouldBe safeId
    }

    "return the error if registration fails" in {
      setupRegister(newRegBadRequest)
      val response = call
      response.isLeft shouldBe true
      response.left.get.status shouldBe BAD_REQUEST
    }

  }

}
