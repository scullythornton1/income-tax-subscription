/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers

import models.ErrorInternalServerError
import play.api.libs.json.Json
import services.SubscriptionService
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController
import common.validation.HeaderValidator
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext.Implicits.global

trait SubscriptionController extends BaseController with HeaderValidator {

  val service: SubscriptionService
  implicit val hc: HeaderCarrier

  val subscribe: Action[AnyContent] = validateAccept(acceptHeaderValidationRules).async {
    service.createSubscription.map(result => result) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }
}