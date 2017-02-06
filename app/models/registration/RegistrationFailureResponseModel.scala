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

package models.registration

import play.api.libs.json._

import scala.util.Try

sealed trait RegistrationResponse

case class RegistrationSuccessResponseModel(safeId: String) extends RegistrationResponse

case class RegistrationFailureResponseModel(reason: String) extends RegistrationResponse

object RegistrationSuccessResponseModel {
  implicit val format = Json.format[RegistrationSuccessResponseModel]
}

object RegistrationFailureResponseModel {
  implicit val format = Json.format[RegistrationFailureResponseModel]
}

object RegistrationResponse {

  lazy val parseFailure = (js: JsValue) => RegistrationFailureResponseModel(s"parse error: $js"):RegistrationResponse

  implicit val reader = new Reads[RegistrationResponse] {

    def reads(js: JsValue): JsResult[RegistrationResponse] = {
      lazy val successResponse = Try(js.asOpt[RegistrationSuccessResponseModel]).getOrElse(None)
      lazy val failureResponse = Try(js.asOpt[RegistrationFailureResponseModel]).getOrElse(None)
      JsSuccess(successResponse.fold(failureResponse.fold(parseFailure(js))(x=>x))(x=>x))
    }
  }
}