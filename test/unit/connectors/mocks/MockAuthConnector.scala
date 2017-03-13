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

package unit.connectors.mocks

import connectors.AuthConnector
import models.auth.{Authority, UserIds}
import org.mockito.Matchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future


trait MockAuthConnector extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneAppPerSuite {

  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  def setupMockCurrentAuthority(authority: Option[Authority]): Unit = {
    when(mockAuthConnector.getCurrentAuthority()(Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(authority))
  }

  val validAuthority = Authority(
    "test.uri", "testGGID", "test.userDetailsLink", UserIds("tiid", "teid")
  )

}
