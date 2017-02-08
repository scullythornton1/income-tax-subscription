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

package models

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class PropertySubscriptionResponseModelSpec extends UnitSpec {
  "Creating a model for a subscription request" should {
    val IncomeModel = IncomeSourcesModel(incomeSourceId = "sourceId0001")
    val ResponseModel = PropertySubscriptionResponseModel(safeId = "XA0001234567890", mtditId = "mdtitId001", incomeSource = IncomeModel)

    "safeId should be XA0001234567890, mtditId should be mdtitId001 and incomeSourcesId should be sourceId0001" in {
      val expected = Json.parse("""{
                                  |
                                  |"safeId": "XA0001234567890",
                                  |
                                  |"mtditId": "mdtitId001",
                                  |
                                  |"incomeSource":
                                  |
                                  |{
                                  |
                                  |"incomeSourceId": "sourceId0001"
                                  |
                                  |}
                                  |
                                  |}""".stripMargin)
      val actual = Json.toJson(ResponseModel)
      actual shouldBe expected
    }
  }
}

