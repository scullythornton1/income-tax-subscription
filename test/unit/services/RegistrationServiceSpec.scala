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
import unit.services.mocks.MockRegistrationService
import utils.TestConstants

class RegistrationServiceSpec extends MockRegistrationService {

  implicit val hc = HeaderCarrier()

  val nino = TestConstants.testNino
  val isAgent = false
  val safeId = TestConstants.testSafeId

  val setupRegister = (setupMockRegister(nino) _).tupled
  val setupGetRegistration = (setupMockGetRegistration(nino) _).tupled


  import TestConstants.{GetRegistrationResponse, NewRegistrationResponse}

  val newRegSuccess = (OK, NewRegistrationResponse.successResponse(safeId))
  val newRegBadRequest = (BAD_REQUEST, NewRegistrationResponse.failureResponse("INVALID_NINO", "Your submission contains one or more errors. Failed Parameter(s) - [idType, idNumber, payload]"))
  val newRegConflict = (CONFLICT, NewRegistrationResponse.failureResponse("CONFLICT", "Duplicate submission"))
  val getRegSuccess = (OK, GetRegistrationResponse.successResponse(safeId))
  val getRegBadRequest = (BAD_REQUEST, GetRegistrationResponse.failureResponse("Your submission contains one or more errors. Failed Parameter(s) - [idType, idNumber, payload]"))

  def call = await(TestRegistrationService.register(isAgent = isAgent, nino))

  "RegistrationService" should {
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

    "return the safeId when the registration is conflict but lookup is successful" in {
      setupRegister(newRegConflict)
      setupGetRegistration(getRegSuccess)
      val response = call
      response.isRight shouldBe true
      response.right.get.safeId shouldBe safeId
    }

    "return the error when both registration is conflict but lookup is unsuccessful" in {
      setupRegister(newRegConflict)
      setupGetRegistration(getRegBadRequest)
      val response = call
      response.isLeft shouldBe true
      response.left.get.status shouldBe BAD_REQUEST
    }
  }

}
