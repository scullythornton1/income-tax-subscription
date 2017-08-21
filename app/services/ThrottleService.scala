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
import org.joda.time.DateTime
import repositories.{Repositories, ThrottleMongoRepository}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.time.DateTimeUtils

import scala.concurrent.Future

sealed trait ThrottleResponse

case class ThrottleSuccessResponse(registrationID: String) extends ThrottleResponse

case object ThrottleTooManyRequestsResponse extends ThrottleResponse

import utils.Implicits._

@Singleton
class ThrottleServiceImp @Inject()(repositories: Repositories,
                                   val logging: Logging) extends ThrottleService with ServicesConfig {
  lazy val throttleMongoRepository = repositories.throttleRepository

  //$COVERAGE-OFF$
  def dateTime: DateTime = DateTimeUtils.now

  lazy val threshold = getConfInt("throttle-threshold", throw new Exception("throttle-threshold not found in config"))
  //$COVERAGE-ON$
}

trait ThrottleService extends BaseController {
  val throttleMongoRepository: ThrottleMongoRepository

  def dateTime: DateTime

  val threshold: Int

  val logging: Logging

  def checkUserAccess(internalId: String): Future[Boolean] = {
    implicit val checkUserAccessLoggingConfig: Option[LoggingConfig] = ThrottleService.checkUserAccessLoggingConfig
    logging.debug(s"Request: internalId=$internalId")
    throttleMongoRepository.checkUserAndUpdate(getCurrentDay, threshold, internalId)
  }

  private[services] def getCurrentDay: String = {
    dateTime.toString("yyyy-MM-dd")
  }

  def dropDb: Future[Unit] = {
    implicit val checkUserAccessLoggingConfig: Option[LoggingConfig] = ThrottleService.dropDbLoggingConfig
    logging.debug(s"Request: dropDb")
    throttleMongoRepository.dropDb
  }

}

object ThrottleService {
  val checkUserAccessLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "ThrottleService.checkUserAccess")
  val dropDbLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "ThrottleService.dropDb")
}
