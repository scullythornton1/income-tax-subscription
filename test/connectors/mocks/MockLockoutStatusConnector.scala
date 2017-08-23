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

package connectors.mocks

import audit.Logging
import config.AppConfig
import connectors.matching.LockoutStatusConnector
import models.ErrorModel
import models.matching.LockoutResponse
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import utils.TestConstants.{testLockoutFailure, testLockoutNone, testLockoutSuccess}

import scala.concurrent.{ExecutionContext, Future}

trait MockLockoutStatusConnector extends MockHttp with GuiceOneAppPerSuite {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val logging: Logging = app.injector.instanceOf[Logging]
  lazy val httpGet: HttpGet = mockHttpGet

  val mockLockoutStatusConnector = mock[LockoutStatusConnector]

  private def mockGetLockoutStatus(arn: String)(result: Future[Either[ErrorModel, Option[LockoutResponse]]]): Unit =
    when(mockLockoutStatusConnector.getLockoutStatus(ArgumentMatchers.eq(arn))(
      ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext]
    )).thenReturn(result)

  def mockLockedOut(arn: String): Unit =
    mockGetLockoutStatus(arn)(Future.successful(testLockoutSuccess))

  def mockNotLockedOut(arn: String): Unit =
    mockGetLockoutStatus(arn)(Future.successful(testLockoutNone))

  def mockLockedOutFailure(arn: String): Unit =
    mockGetLockoutStatus(arn)(Future.successful(testLockoutFailure))

  //  object TestLockoutStatusConnector extends LockoutStatusConnector(appConfig, logging, httpGet)


}
