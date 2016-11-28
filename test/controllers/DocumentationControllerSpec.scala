/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers

import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.io.Source

class DocumentationControllerSpec extends UnitSpec with WithFakeApplication {

  val definitionSource: String = Source.fromFile("resources/public/api/definition.json").getLines.mkString
  val expectedJsonDefinition: JsValue = Json.parse(definitionSource)

  "Calling the .definition method" should {
    lazy val result = DocumentationController.definition(FakeRequest())

    "return Status OK (200)" in {
      status(result) shouldBe 200
    }

    "return the Json Definition response" in {
      jsonBodyOf(await(result)) shouldBe expectedJsonDefinition
    }
  }
}
