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

import models.gg.{KnownFactsRequest, TypeValuePair}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.JsonUtils._
import utils.TestConstants.GG.TypeValuePairExamples

class KnownFactsRequestSpec extends UnitSpec {

  import TypeValuePairExamples._

  "KnownFactsRequest" should {
    "Provide the correct writer for KnownFactsRequest" in {
      val knownFact1 = TypeValuePair(
        `type` = testType1,
        value = testValue1
      )
      val knownFact2 = TypeValuePair(
        `type` = testType2,
        value = testValue2
      )
      val knownFactsRequest = KnownFactsRequest(List(knownFact1, knownFact2))

      val request: JsValue = Json.toJson(knownFactsRequest)
      val expected = Json.fromJson[KnownFactsRequest](
        s"""{
           | "facts": [
           |        ${jsonTypeValuePair(testType1, testValue1).get},
           |        ${jsonTypeValuePair(testType2, testValue2).get}]
           | }""".stripMargin).get
      val actual = Json.fromJson[KnownFactsRequest](request).get

      actual shouldBe expected
    }
  }

}
