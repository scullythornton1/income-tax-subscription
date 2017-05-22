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

package helpers.servicemocks

import helpers.WiremockHelper
import play.api.http.Status
import utils.TestConstants

object AuthStub {
  val idsLink = "/uri/to/ids"
  val getAuthorityURI = "/auth/authority"

  def stubGetAuthoritySuccess(): Unit = {
    val authBody = TestConstants.Auth.authResponseJson("/auth/oid/58a2e8c82e00008c005d4699", "/uri/to/user-details", "12345", idsLink).toString()

    WiremockHelper.stubGet(getAuthorityURI, Status.OK, authBody)
  }

  def stubGetAuthorityFailure(): Unit = {
    WiremockHelper.stubGet(getAuthorityURI, Status.UNAUTHORIZED, "")
  }

  def stubGetIDsSuccess(): Unit = {
    val idsBody = TestConstants.Auth.idsResponseJson("foo", "bar").toString()

    WiremockHelper.stubGet(idsLink, Status.OK, idsBody)
  }
}
