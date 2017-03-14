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

package it.services

import it.services.its.ITUserAccessService
import models.throttling.{CanAccess, LimitReached}
import uk.gov.hmrc.play.http.HeaderCarrier

class UserAccessServiceSpec extends ITUserAccessService {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach: Unit = {
    super.beforeEach
    await(TestUserAccessService.dropDb)
  }

  override def afterEach: Unit = {
    super.afterEach
    await(TestUserAccessService.dropDb)
  }

  "UserAccessService.dropDb" should {
    "clear the mongo repo" in {
      val f = TestUserAccessService.dropDb
      await(f) shouldBe ((): Unit)
      await(TestRepositories.throttleRepository.collectionExists) shouldBe false
    }
  }

  "UserAccessService.checkUserAccess" should {

    "return CanAccess when the user can gain access" in {
      await(TestUserAccessService.checkUserAccess(1.toString)) shouldBe CanAccess

      // populate the users to the threshold limit
      for (i <- 1 to TestThrottleService.threshold) {
        await(TestThrottleService.checkUserAccess(i.toString))
      }

      await(TestRepositories.throttleRepository.userCount) shouldBe TestThrottleService.threshold

      // upon revisit the user should still be allowed entry
      await(TestUserAccessService.checkUserAccess("1")) shouldBe CanAccess
    }

    "return LimitReached for a new user if a daily limit has been reached" in {
      // populate the users to the threshold limit
      for (i <- 1 to TestThrottleService.threshold) {
        await(TestThrottleService.checkUserAccess(i.toString))
      }

      await(TestRepositories.throttleRepository.userCount) shouldBe TestThrottleService.threshold
      await(TestUserAccessService.checkUserAccess((TestThrottleService.threshold + 1).toString)) shouldBe LimitReached
    }
  }

}
