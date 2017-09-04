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

import models.ErrorModel
import models.frontend.{FERequest, FESuccessResponse}
import models.subscription.IncomeSourceModel
import models.subscription.property.PropertySubscriptionResponseModel
import play.api.http.Status._
import services.mocks.TestSubscriptionManagerService
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Right
import uk.gov.hmrc.http.HeaderCarrier

class RosmAndEnrolManagerServiceSpec extends TestSubscriptionManagerService {

  implicit val hc = HeaderCarrier()
  implicit val ec = ExecutionContext.Implicits.global

  val path = ""

  "The RosmAndEnrolManagerService.rosmAndEnrol action" should {

    def call(request: FERequest): Either[ErrorModel, FESuccessResponse] = await(TestSubscriptionManagerService.rosmAndEnrol(request, path))

    "return the mtditId when register and subscribe are successful (property only)" in {
      val propertySubscriptionSuccess = PropertySubscriptionResponseModel(testSafeId, testMtditId, IncomeSourceModel(testSourceId))

      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(fePropertyRequest)(Future.successful(Right(propertySubscriptionSuccess)))
      call(fePropertyRequest).right.get.mtditId.get shouldBe testMtditId
    }

    "return the mtditId when register and subscribe are successful (business only)" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockBusinessSubscribe(feBusinessRequest)(Future.successful(Right(businessSubscriptionSuccess)))
      call(feBusinessRequest).right.get.mtditId.get shouldBe testMtditId
    }

    "return the mtditId when register and subscribe are successful (both business and property)" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(feBothRequest)(Future.successful(Right(propertySubscriptionSuccess)))
      mockBusinessSubscribe(feBothRequest)(Future.successful(Right(businessSubscriptionSuccess)))
      call(feBothRequest).right.get.mtditId.get shouldBe testMtditId
    }

    "return an error when reg, property subscribe success, business subscribe fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(feBothRequest)(Future.successful(Right(propertySubscriptionSuccess)))
      mockBusinessSubscribe(feBothRequest)(Left(NOT_FOUND_NINO_MODEL))
      call(feBothRequest).left.get.status shouldBe NOT_FOUND
    }

    "return an error when reg, property subscribe fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(feBothRequest)(Future.successful(Left(NOT_FOUND_NINO_MODEL)))
      mockBusinessSubscribe(feBothRequest)(Future.successful(Left(NOT_FOUND_NINO_MODEL)))
      call(feBothRequest).left.get.status shouldBe NOT_FOUND
    }

    "return an error when reg fails" in {
      mockRegister(registerRequestPayload)(UNAVAILABLE)
      call(feBothRequest).left.get.status shouldBe SERVICE_UNAVAILABLE
    }
  }

  "The RosmAndEnrolManagerService.orchestrateROSM action" should {

    def call(request: FERequest): Either[ErrorModel, FESuccessResponse] = await(TestSubscriptionManagerService.orchestrateROSM(request))

    "return the mtditID when registration and subscription for property is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(fePropertyRequest)(Future.successful(Right(propertySubscriptionSuccess)))
      call(fePropertyRequest).right.get.mtditId.get shouldBe testMtditId
    }

    "return the mtditID when registration and subscription for business is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockBusinessSubscribe(feBusinessRequest)(Future.successful(Right(businessSubscriptionSuccess)))
      call(feBusinessRequest).right.get.mtditId.get shouldBe testMtditId
    }

    "return the mtditID when registration and subscription for both Property and Business is successful" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(feBothRequest)(Future.successful(Right(propertySubscriptionSuccess)))
      mockBusinessSubscribe(feBothRequest)(Future.successful(Right(businessSubscriptionSuccess)))
      call(feBothRequest).right.get.mtditId.get shouldBe testMtditId
    }

    "return the error if registration fails" in {
      mockRegister(registerRequestPayload)(INVALID_NINO)
      call(fePropertyRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration successful but property subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(fePropertyRequest)(Future.successful(Left(INVALID_NINO_MODEL)))
      call(fePropertyRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration successful but business subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockBusinessSubscribe(feBusinessRequest)(Future.successful(Left(INVALID_NINO_MODEL)))
      call(feBusinessRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration and property successful, but business subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(feBothRequest)(Future.successful(Right(propertySubscriptionSuccess)))
      mockBusinessSubscribe(feBothRequest)(Future.successful(Left(INVALID_NINO_MODEL)))
      call(feBothRequest).left.get.status shouldBe BAD_REQUEST
    }

    "return the error if registration and business successful, but property subscription fails" in {
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(feBothRequest)(Future.successful(Left(INVALID_NINO_MODEL)))
      mockBusinessSubscribe(feBothRequest)(Future.successful(Right(businessSubscriptionSuccess)))
      call(feBothRequest).left.get.status shouldBe BAD_REQUEST
    }

  }

}
