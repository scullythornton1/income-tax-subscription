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

package repositories

import helpers.IntegrationTestConstants._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import repositories.digitalcontact.PaperlessPreferenceMongoRepository
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits._

class PaperlessPreferenceMongoRepositorySpec extends UnitSpec with GuiceOneAppPerSuite with BeforeAndAfterEach {
  val TestPaperlessPreferenceMongoRepository = app.injector.instanceOf[PaperlessPreferenceMongoRepository]

  override def beforeEach(): Unit = {
    await(TestPaperlessPreferenceMongoRepository.drop)
  }

  "storeNinno" should {
    "return the model when it is successfully inserted" in {
      val (insertRes, stored) = await(for {
        insertRes <- TestPaperlessPreferenceMongoRepository.storeNino(testPaperlessPreferenceKey)
        stored <- TestPaperlessPreferenceMongoRepository.find("_id" -> testPreferencesToken)
      } yield (insertRes, stored))

      stored.size shouldBe 1
      insertRes shouldBe stored.head
    }

    "fail when a duplicate is created" in {
      val res = for {
        _ <- TestPaperlessPreferenceMongoRepository.storeNino(testPaperlessPreferenceKey)
        _ <- TestPaperlessPreferenceMongoRepository.storeNino(testPaperlessPreferenceKey)
      } yield ()

      intercept[Exception](await(res))
    }
  }

  "getLockoutStatus" should {
    "return None when there is no lock" in {
      val res = await(TestPaperlessPreferenceMongoRepository.retrieveNino(testPreferencesToken))
      res shouldBe empty
    }

    "return a LockModel if there is a lock" in {
      val res = await(for {
        insertRes <- TestPaperlessPreferenceMongoRepository.storeNino(testPaperlessPreferenceKey)
        stored <- TestPaperlessPreferenceMongoRepository.retrieveNino(testPreferencesToken)
      } yield stored)

      res shouldBe Some(testPaperlessPreferenceKey)
    }
  }
}
