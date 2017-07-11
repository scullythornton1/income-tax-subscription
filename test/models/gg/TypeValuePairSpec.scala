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

package models.gg

import models.gg.TypeValuePair
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants.GG.TypeValuePairExamples

class TypeValuePairSpec extends UnitSpec {

  import TypeValuePairExamples._

  "TypeValuePair" should {
    "Provide the correct writer for TypeValuePair" in {
      val knownFact = TypeValuePair(
        `type` = testType1,
        value = testValue1
      )

      val request: JsValue = Json.toJson(knownFact)
      val expected = Json.fromJson[TypeValuePair](jsonTypeValuePair(testType1,testValue1)).get
      val actual = Json.fromJson[TypeValuePair](request).get

      actual shouldBe expected
    }
  }

}
