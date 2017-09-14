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

import services.mocks.TestPaperlessPreferenceService
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._

class PaperlessPreferenceServiceSpec extends UnitSpec with TestPaperlessPreferenceService {
  "storeNino" should {
    "return the model when it is successfully stored" in {
      mockNinoStore(testPaperlessPreferenceKey)

      val res = TestPaperlessPreferenceService.storeNino(testPaperlessPreferenceKey)
      await(res) shouldBe testPaperlessPreferenceKey
    }

    "return the failure when the storage fails" in {
      mockNinoStoreFailed(testPaperlessPreferenceKey)

      val res = TestPaperlessPreferenceService.storeNino(testPaperlessPreferenceKey)
      intercept[Exception](await(res)) shouldBe testException
    }
  }
}
