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
import it.services.TestUserAccessService
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import services.MetricsService
import uk.gov.hmrc.play.test.UnitSpec

class UserAccessControllerSpec extends UnitSpec with MockitoSugar with OneServerPerSuite {

  val mockMetrics: MetricsService = mock[MetricsService]

  object TestUserAccessController extends UserAccessController(
    mockMetrics,
    TestUserAccessService) {}

  // TODO


}