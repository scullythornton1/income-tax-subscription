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

package controllers

import play.api.test.FakeRequest
import services.SandboxSubscriptionService
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.mvc.Http.HeaderNames._
import play.mvc.Http.Status._

class SubscriptionControllerSpec extends UnitSpec with WithFakeApplication {

  object testController extends SubscriptionController {
    override val service = SandboxSubscriptionService
    override implicit val hc = HeaderCarrier()
  }

  "Calling the .subscribe method of the SubscriptionController" when {

    "No accept header is suppliied" should {

      lazy val result = testController.subscribe(FakeRequest())

      "return status NOT_ACCEPTABLE (406)" in {
        status(result) shouldBe NOT_ACCEPTABLE
      }
    }

    "A valid Accept header is supplied" should {

      lazy val result = testController.subscribe(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json")))

      "return status CREATED (201)" in {
        status(result) shouldBe CREATED
      }
    }
  }
}
