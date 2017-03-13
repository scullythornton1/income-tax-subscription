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

package unit.auth

import auth.{Authenticated, AuthenticationResult, LoggedIn, NotLoggedIn}
import connectors.AuthConnector
import play.api.http.Status._
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.connectors.mocks.MockAuthConnector
import utils.Implicits._

import scala.concurrent.Future

class AuthenticatedSpec extends MockAuthConnector {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestAuthenticated extends Authenticated {
    override lazy val auth: AuthConnector = mockAuthConnector
  }

  val testFunc: AuthenticationResult => Future[Result] = {
    case NotLoggedIn => Future.successful(BadRequest(""))
    case LoggedIn(_) => Future.successful(Ok(""))
  }

  def call: Future[Result] = TestAuthenticated.authenticated(testFunc)

  "Authenticated.authenticated" should {
    "return NotLoggedIn" in {
      setupMockCurrentAuthority(None)
      val r = call
      status(r) shouldBe BAD_REQUEST
    }

    "return LoggedIn" in {
      setupMockCurrentAuthority(validAuthority)
      val r = call
      status(r) shouldBe OK
    }
  }

}
