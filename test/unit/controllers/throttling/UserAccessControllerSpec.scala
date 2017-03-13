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

package unit.controllers.throttling

import controllers.throttling.UserAccessController
import models.throttling.{CanAccess, LimitReached}
import play.api.http.Status._
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.FakeRequest
import services.MetricsService
import unit.connectors.mocks.MockAuthConnector
import unit.controllers.throttling.mocks.MockUserAccessService
import utils.Implicits._
import utils.TestConstants

import scala.concurrent.Future

class UserAccessControllerSpec extends MockUserAccessService
  with MockAuthConnector {

  object TestUserAccessController extends UserAccessController(
    app.injector.instanceOf[MetricsService],
    mockUserAccessService,
    mockAuthConnector) {
  }

  def call(request: Request[AnyContentAsEmpty.type]): Future[Result] = TestUserAccessController.checkUserAccess(TestConstants.testNino)(request)

  "UserAccessController" should {
    "if a user is not logged in return forbidden" in {
      val request = FakeRequest()
      setupMockCurrentAuthority(None)
      val r = call(request)
      status(r) shouldBe FORBIDDEN
    }

    "if a user is logged and within daily limit in return OK" in {
      val request = FakeRequest()
      setupMockCurrentAuthority(validAuthority)
      setupMockCheckUserAccess(CanAccess)
      val r = call(request)
      status(r) shouldBe OK
    }

    "if a user is logged but exceeds daily limit in return OK" in {
      val request = FakeRequest()
      setupMockCurrentAuthority(validAuthority)
      setupMockCheckUserAccess(LimitReached)
      val r = call(request)
      status(r) shouldBe TOO_MANY_REQUESTS
    }
  }

}
