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

package controllers.subscription

import javax.inject.Inject

import audit.{Logging, LoggingConfig}
import connectors.AuthConnector
import controllers.AuthenticatedController
import models.frontend.FEFailureResponse
import play.api.mvc.{Action, AnyContent}
import services.SubscriptionStatusService
import utils.Implicits._
import utils.JsonUtils.toJsValue

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionStatusController @Inject()(logging: Logging,
                                             override val auth: AuthConnector,
                                             subscriptionStatusService: SubscriptionStatusService) extends AuthenticatedController {

  def checkSubscriptionStatus(nino: String): Action[AnyContent] = Action.async { implicit request =>
    authenticated {
      implicit val loggingConfig = SubscriptionStatusController.checkSubscriptionStatusLoggingConfig
      subscriptionStatusService.checkMtditsaSubscription(nino).map {
        case Right(r) =>
          logging.debug(s"successful, responding with\n$r")
          Ok(toJsValue(r))
        case Left(l) =>
          logging.warn(s"failed, responding with\nstatus=${l.status}\nreason=${l.reason}")
          Status(l.status)(toJsValue(FEFailureResponse(l.reason)))
      }
    }
  }

}

object SubscriptionStatusController {
  val checkSubscriptionStatusLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionStatusController.checkSubscriptionStatus")
}
