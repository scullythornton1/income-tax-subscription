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

package unit.models.frontend

import models.frontend
import models.frontend.{FERequest, _}
import play.api.libs.json.{JsError, JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.JsonUtils._
import utils.TestConstants

class FERequestSpec extends UnitSpec {

  "FERequest" should {
    "Provide the correct reader for FERequest, set enrolUser to true if it is not provided" in {
      val feRequest = FERequest(
        nino = TestConstants.testNino,
        incomeSource = Business,
        enrolUser = true
      )

      val request: JsValue = Json.toJson(feRequest)
      val expected = Json.fromJson[FERequest](
        s"""{"nino" : "${TestConstants.testNino}",
           | "isAgent" : false,
           | "incomeSource":"${IncomeSourceType.business}"}""".stripMargin).get
      val actual = Json.fromJson[FERequest](request).get
      actual shouldBe expected
    }

    "Provide the correct reader for FERequest, set enrolUser to the supplied value if it is provided" in {
      val feRequest = FERequest(
        nino = TestConstants.testNino,
        incomeSource = Business,
        enrolUser = false
      )

      val request: JsValue = Json.toJson(feRequest)
      val expected = Json.fromJson[FERequest](
        s"""{"nino" : "${TestConstants.testNino}",
           | "isAgent" : false,
           | "incomeSource":"${IncomeSourceType.business}",
           | "enrolUser" : false
           |}""".stripMargin).get
      val actual = Json.fromJson[FERequest](request).get
      actual shouldBe expected
    }

    "be be invalid if an agent does not have an arn" in {
      val parsed = Json.fromJson[FERequest](
        s"""{"nino" : "${TestConstants.testNino}",
           | "isAgent" : true,
           | "incomeSource":"${IncomeSourceType.business}",
           | "enrolUser" : false
           |}""".stripMargin)
      parsed.isError shouldBe true
      parsed match {
        case e: JsError => JsError.toJson(e).toString should include(FERequest.agentWithoutArnErrMsg)
      }
    }

    "be be valid if an agent does have an arn" in {
      val parsed = Json.fromJson[FERequest](
        s"""{"nino" : "${TestConstants.testNino}",
           | "isAgent" : true,
           | "arn" : "${TestConstants.testArn}",
           | "incomeSource":"${IncomeSourceType.business}",
           | "enrolUser" : false
           |}""".stripMargin)
      parsed.isSuccess shouldBe true
    }

    //n.b. the valid case for none agent is covered by the first couple of tests above
    "be be invalid if a none agent has an arn" in {
      val parsed = Json.fromJson[FERequest](
        s"""{"nino" : "${TestConstants.testNino}",
           | "isAgent" : false,
           |  "arn" : "${TestConstants.testArn}",
           | "incomeSource":"${IncomeSourceType.business}",
           | "enrolUser" : false
           |}""".stripMargin)
      parsed.isError shouldBe true
      parsed match {
        case e: JsError => JsError.toJson(e).toString should include(FERequest.noneAgentWithArnErrMsg)
      }
    }

  }
}
