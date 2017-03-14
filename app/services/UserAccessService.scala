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

package services

import javax.inject.{Inject, Singleton}

import audit.{Logging, LoggingConfig}
import models.throttling._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NoStackTrace


private[services] class MissingRegistration(regId: String) extends NoStackTrace

@Singleton
class UserAccessService @Inject()(val throttleService: ThrottleService,
                                  logging: Logging) extends ServicesConfig {

  def checkUserAccess(internalId: String)(implicit hc: HeaderCarrier): Future[UserAccess] = {
    implicit val checkUserAccessLoggingConfig: Option[LoggingConfig] = UserAccessService.checkUserAccessLoggingConfig
    logging.debug(s"Request: internalId=$internalId")
    throttleService.checkUserAccess(internalId) flatMap {
      case true => CanAccess
      case false => LimitReached
    }
  }

  def dropDb: Future[Unit] = {
    implicit val checkUserAccessLoggingConfig: Option[LoggingConfig] = UserAccessService.dropDbLoggingConfig
    logging.debug(s"Request: dropDb")
    throttleService.dropDb
  }

}

object UserAccessService {
  val checkUserAccessLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "UserAccessService.checkUserAccess")
  val dropDbLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "UserAccessService.dropDb")
}