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

import _root_.it.repositories.TestRepositories
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec

class ThrottleServiceSpec extends UnitSpec
  with MockitoSugar
  with BeforeAndAfterEach
  with OneAppPerSuite {

  override def beforeEach: Unit = {
    super.beforeEach
    await(TestThrottleService.dropDb)
  }

  override def afterEach: Unit = {
    super.afterEach
    await(TestThrottleService.dropDb)
  }

  "ThrottleService" should {
    "allow clear db" in {
      val f = TestThrottleService.dropDb
      await(f) shouldBe()
      await(TestRepositories.throttleRepository.collectionExists) shouldBe false
    }

    "should add users up to the maximum allowed number of users" in {
      await(TestRepositories.throttleRepository.collectionExists) shouldBe false
      await(TestRepositories.throttleRepository.userCount) shouldBe 0

      for (i <- 1 until TestThrottleService.threshold) {
        val accessGranted = await(TestThrottleService.checkUserAccess(i.toString))

        await(TestRepositories.throttleRepository.collectionExists) shouldBe true

        val collectionCount = await(TestRepositories.throttleRepository.userCount)
        withClue(s"mongo db expections did not match\ni=$i\ncollectionCount=$collectionCount\nexpected count=$i\n") {
          accessGranted shouldBe true
          collectionCount shouldBe i
        }
      }

      val f = TestThrottleService.checkUserAccess((TestThrottleService.threshold + 1).toString)
      await(f) shouldBe false
      await(TestRepositories.throttleRepository.userCount) shouldBe TestThrottleService.threshold

      // now validate the previous users are allowed back in
      for (i <- 1 until TestThrottleService.threshold) {
        val accessGranted = await(TestThrottleService.checkUserAccess(i.toString))

        await(TestRepositories.throttleRepository.collectionExists) shouldBe true
        accessGranted shouldBe true

        val collectionCount = await(TestRepositories.throttleRepository.userCount)

        collectionCount shouldBe TestThrottleService.threshold
      }
    }

    "duplicated users would not increment the count" in {
      await(TestRepositories.throttleRepository.collectionExists) shouldBe false
      await(TestRepositories.throttleRepository.userCount) shouldBe 0

      // add the same user multiple times
      for (i <- 1 to 2) {
        val accessGranted = await(TestThrottleService.checkUserAccess("1"))

        await(TestRepositories.throttleRepository.collectionExists) shouldBe true
        accessGranted shouldBe true

        val collectionCount = await(TestRepositories.throttleRepository.userCount)

        // this should not increase the count
        collectionCount shouldBe 1
      }

    }
  }

}
