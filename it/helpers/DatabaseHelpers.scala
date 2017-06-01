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

package helpers

import models.throttling.UserCount
import helpers.servicemocks.AuthStub._
import uk.gov.hmrc.time.DateTimeUtils

object DatabaseHelpers {
  val id = DateTimeUtils.now.toString("yyyy-MM-dd")

  val maxUserCount = UserCount(id, Set("otherID1", "otherID2"), 0)
  val matchingUserCount = UserCount(id, Set(internalID, "otherID"), 0)
  val nonFullUserCount = UserCount(id, Set("otherID"), 0)
}
