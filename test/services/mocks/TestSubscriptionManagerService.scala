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

package services.mocks

import audit.Logging
import config.AppConfig
import models.ErrorModel
import models.frontend.{FERequest, FESuccessResponse}
import org.mockito.ArgumentMatchers
import org.scalatest.mockito.MockitoSugar
import services.RosmAndEnrolManagerService
import org.mockito.Mockito._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{ HeaderCarrier, HttpGet }

trait MockSubscriptionManagerService extends MockitoSugar {
  val mockSubscriptionManagerService = mock[RosmAndEnrolManagerService]

  def mockRosmAndEnrol(request: FERequest, path: String)(response: Future[Either[ErrorModel, FESuccessResponse]]): Unit = {
    when(mockSubscriptionManagerService.rosmAndEnrol(
      ArgumentMatchers.eq(request),
      ArgumentMatchers.eq(path)
    )(
      ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext]
    ))
      .thenReturn(response)
  }

}

trait TestSubscriptionManagerService extends MockRegistrationService with MockSubscriptionService {

  override lazy val appConfig = app.injector.instanceOf[AppConfig]
  override lazy val logging = app.injector.instanceOf[Logging]
  override lazy val httpPost = mockHttpPost
  override lazy val httpGet: HttpGet = mockHttpGet

  object TestSubscriptionManagerService extends RosmAndEnrolManagerService(
    appConfig,
    logging,
    TestRegistrationService,
    mockSubscriptionService
  )

}
