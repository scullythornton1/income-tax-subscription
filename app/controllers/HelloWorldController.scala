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
import services.HelloWorldService
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController
import common.validation.HeaderValidator
import scala.concurrent.ExecutionContext.Implicits.global

trait HelloWorldController extends BaseController with HeaderValidator {
  val service: HelloWorldService
  implicit val hc: HeaderCarrier

  val world = validateAccept(acceptHeaderValidationRules).async {
    service.fetchWorld.map(as => Ok(Json.toJson(as))
    ) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }

  val application = validateAccept(acceptHeaderValidationRules).async {
    service.fetchApplication.map(as => Ok(Json.toJson(as))
    ) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }

  val user = validateAccept(acceptHeaderValidationRules).async {
    service.fetchUser.map(as => Ok(Json.toJson(as))
    ) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }
}
