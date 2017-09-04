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

package services

import play.api.http.Status._
import services.mocks.MockRegistrationService
import utils.TestConstants._
import uk.gov.hmrc.http.HeaderCarrier

class RegistrationServiceSpec extends MockRegistrationService {

  implicit val hc = HeaderCarrier()

  def call = await(TestRegistrationService.register(isAgent = false, testNino))

  "RegistrationService" should {

    "return the safeId when the registration is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      call.right.get.safeId shouldBe testSafeId
    }

    "return the error if registration fails" in {
      mockRegister(registerRequestPayload)(INVALID_NINO)
      call.left.get.status shouldBe BAD_REQUEST
    }

    "return the safeId when the registration is conflict but lookup is successful" in {
      mockRegister(registerRequestPayload)(CONFLICT_ERROR)
      mockGetRegistration(getRegSuccess)
      call.right.get.safeId shouldBe testSafeId
    }

    "return the error when both registration is conflict but lookup is unsuccessful" in {
      mockRegister(registerRequestPayload)(CONFLICT_ERROR)
      mockGetRegistration(INVALID_NINO)
      call.left.get.status shouldBe BAD_REQUEST
    }
  }

}
