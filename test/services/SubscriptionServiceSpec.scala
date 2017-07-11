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

import models.subscription.property.PropertySubscriptionResponseModel
import models.subscription.IncomeSourceModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.JsonUtils
import utils.TestConstants._
import services.mocks.MockSubscriptionService

import scala.util.Right

class SubscriptionServiceSpec extends UnitSpec with OneAppPerSuite with JsonUtils with MockSubscriptionService {

  implicit val hc = HeaderCarrier()

  "SubscriptionService" should {

    def propertySubscribeCall = await(TestSubscriptionService.propertySubscribe(fePropertyRequest))

    "return success if the subscription succeeds" in {
      mockPropertySubscribe(propertySubscribeSuccess)
      val expected = PropertySubscriptionResponseModel(testSafeId, testMtditId, IncomeSourceModel(testSourceId))
      propertySubscribeCall shouldBe Right(expected)
    }

    "return not found response" in {
      mockPropertySubscribe(NOT_FOUND_NINO)
      propertySubscribeCall shouldBe Left(NOT_FOUND_NINO_MODEL)
    }
  }

}
