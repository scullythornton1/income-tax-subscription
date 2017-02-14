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

package unit.controllers.subscription

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import controllers.subscription.SubscriptionController
import models.frontend.{Both, FERequest, FESuccessResponse, Property}
import play.api.Application
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsJson, Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import unit.services.mocks.MockSubscriptionManagerService
import utils.JsonUtils._
import utils.MaterializerSupport
import utils.TestConstants._

import scala.concurrent.Future

class SubscriptionControllerSpec extends UnitSpec with MockSubscriptionManagerService with MaterializerSupport {

  val application = app.injector.instanceOf[Application]

  object TestController extends SubscriptionController(application, TestSubscriptionManagerService)

  def call(request: Request[AnyContentAsJson]): Future[Result] = TestController.subscribe(testNino)(request)

  "SubscriptionController" should {
    "return the id when successful" in {
      val feRequest: JsValue = FERequest(testNino, incomeSource = Property)
      val fakeRequest: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(feRequest)
      mockRegister(registerRequestPayload)(regSuccess)
      mockPropertySubscribe(propertySubscribeSuccess)
      val result = call(fakeRequest)
      jsonBodyOf(result).as[FESuccessResponse].mtditId shouldBe testMtditId
    }

    "return failure when it's unsuccessful" in {
      val fakeRequest: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody("{}")
      val result = call(fakeRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }

}
