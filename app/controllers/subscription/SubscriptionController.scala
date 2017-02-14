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

import audit.{LoggingConfig, Logging}
import models.frontend.{FEFailureResponse, FERequest, FESuccessResponse}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, Result}
import services.SubscriptionManagerService
import uk.gov.hmrc.play.microservice.controller.BaseController
import utils.JsonUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionController @Inject()(logging: Logging,
                                       subManService: SubscriptionManagerService) extends BaseController {

  def subscribe(nino: String): Action[AnyContent] = Action.async {
    implicit request =>
      implicit val loggingConfig = SubscriptionController.subscribeLoggingConfig
      logging.info(s"Request received for $nino")
      lazy val parseError: Future[Result] = BadRequest(FEFailureResponse("Request is invalid"): JsValue)
      request.body.asJson.fold(parseError) { x =>
        parseUtil(x)(FERequest.format).fold(
          invalid => {
            // This has been set to err because it should never happen as it would imply our frontend is not configured
            // to talk to this service in the correct manner
            logging.err(s"Request is invalid:\n${invalid.toString}\n${request.body.toString}")
            parseError
          },
          feRequest => subManService.subscribe(feRequest).map {
            case Right(r) =>
              val response: JsValue = FESuccessResponse("1234567")
              logging.info(s"Responded with $response")
              Ok(response)
            case Left(l) =>
              logging.debug(s"Responding with\nstatus=${l.status}\nreason=${l.reason}")
              Status(l.status)(FEFailureResponse(l.reason): JsValue)
          }
        )
      }
  }

}

object SubscriptionController {
  val subscribeLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionController.subscribe")
}
