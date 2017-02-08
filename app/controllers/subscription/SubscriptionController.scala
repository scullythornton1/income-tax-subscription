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

import models.frontend.{FEFailureResponse, FERequest, FESuccessResponse}
import play.api.Application
import play.api.libs.json.{JsResult, JsValue}
import play.api.mvc.{Action, AnyContent, Result}
import services.RegistrationService
import uk.gov.hmrc.play.microservice.controller.BaseController
import utils.JsonUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionController @Inject()(application: Application,
                                       registrationService: RegistrationService) extends BaseController {

  def subscribe: Action[AnyContent] = Action.async {
    implicit request =>
      lazy val parseError: Future[Result] = BadRequest(FEFailureResponse("Request is invalid"): JsValue)
      request.body.asJson.fold(parseError) { x =>
        val parsedJson: JsResult[FERequest] = parseUtil(x)(FERequest.format)
        parsedJson.fold(
          invalid => parseError,
          valid => {
            val response = registrationService.register(isAgent = valid.isAgent, nino = valid.nino)
            response map {
              //TODO frontend response
              case Right(r) => Ok(FESuccessResponse("1234567"): JsValue)
              case Left(l) => Status(l.status)(FEFailureResponse(l.reason): JsValue)
            }
          }
        )
      }
  }

}
