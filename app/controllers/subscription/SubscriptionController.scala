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
import services.SubscriptionManagerService
import uk.gov.hmrc.play.microservice.controller.BaseController
import utils.JsonUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionController @Inject()(application: Application,
                                       subManService: SubscriptionManagerService) extends BaseController {

  def subscribe(nino: String): Action[AnyContent] = Action.async {
    implicit request =>
      lazy val parseError: Future[Result] = BadRequest(FEFailureResponse("Request is invalid"): JsValue)
      request.body.asJson.fold(parseError) { x =>



        println("")
        println("")
        println("")
        println("####### REQUEST ########")
        println("")
        println(x)
        println("")
        println("")
        println("")
        println("")

        parseUtil(x)(FERequest.format).fold(
          invalid => parseError,
          feRequest => subManService.orchestrateSubscription(feRequest).map {
            //TODO frontend response
            case Right(r) => {
              println(s"!!!!!! OMG it worked. Ref=${r.mtditId}")
              Ok(r: JsValue)
            }
            case Left(l) => {
              println(s"!!!!!! OMG I died!!!!!!!!!!!!!!!")
              Status(l.status)(FEFailureResponse(l.reason): JsValue)
            }
          }
        )
      }
  }

}
