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

package connectors.matching

import javax.inject.Inject

import audit.Logging
import config.AppConfig
import connectors.RawResponseReads
import models.ErrorModel
import models.matching.LockoutResponse
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.{ExecutionContext, Future}

class LockoutStatusConnector @Inject()(appConfig: AppConfig,
                                       logging: Logging,
                                       httpGet: HttpGet
                                      ) extends ServicesConfig with RawResponseReads {

  import Logging._

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  def getLockoutStatus(arn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, Option[LockoutResponse]]] = ???

}
