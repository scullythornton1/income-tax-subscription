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
import models.frontend.{FEFailureResponse, FERequest}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.SubscriptionManagerService
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController
import utils.JsonUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionController @Inject()(logging: Logging,
                                       subManService: SubscriptionManagerService) extends BaseController {

  def subscribe(nino: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val loggingConfig = SubscriptionController.subscribeLoggingConfig
    logging.debug(s"Request received for $nino")
    lazy val parseError: Future[Result] = BadRequest(toJsValue(FEFailureResponse("Request is invalid")))

    parseRequest(request).fold(parseError) { feRequest =>
      createSubscription(feRequest)
    }
  }

  private def parseRequest(request: Request[AnyContent]): Option[FERequest] = request.body.asJson.fold[Option[FERequest]](None) {
    implicit val loggingConfig = SubscriptionController.parseRequestLoggingConfig
    jsonBody => parseUtil(jsonBody)(FERequest.format).fold(
      invalid => {
        logging.err(s"Request is invalid:\n${invalid.toString}\n${request.body.toString}")
        None
      },
      valid => Some(valid)
    )
  }

  private def createSubscription(feRequest: FERequest)(implicit hc: HeaderCarrier): Future[Result] = subManService.orchestrateSubscription(feRequest).map {
    case Right(r) =>
      logging.debug(s"Subscription successful, responding with\n$r")
      Ok(toJsValue(r))
    case Left(l) =>
      logging.warn(s"Subscription failed, responding with\nstatus=${l.status}\nreason=${l.reason}")
      Status(l.status)(toJsValue(FEFailureResponse(l.reason)))
  }

}

object SubscriptionController {
  val subscribeLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionController.subscribe")
  val parseRequestLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionController.parseRequest")
}
