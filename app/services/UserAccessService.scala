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

import javax.inject.Inject

import models.throttling._
import repositories.Repositories
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NoStackTrace


class UserAccessServiceImp @Inject()(repositories: Repositories,
                                     throttleServ: ThrottleService) extends UserAccessService with ServicesConfig {
  val throttleService = throttleServ
  //$COVERAGE-OFF$
  val threshold = getConfInt("throttle-threshold", throw new Exception("Could not find Threshold in config"))
  //$COVERAGE-ON$
}

private[services] class MissingRegistration(regId: String) extends NoStackTrace

trait UserAccessService {

  val threshold: Int
  val throttleService: ThrottleService

  def checkUserAccess(internalId: String)(implicit hc: HeaderCarrier): Future[UserAccess] =
    throttleService.checkUserAccess(internalId) flatMap {
      case true => CanAccess
      case false => LimitReached
    }

}
