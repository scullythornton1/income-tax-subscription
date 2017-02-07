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
import utils.Implicits


case class RegistrationSuccessResponseModel(safeId: String)

case class RegistrationFailureResponseModel(reason: String)

object RegistrationSuccessResponseModel {
  implicit val format = Json.format[RegistrationSuccessResponseModel]
}

object RegistrationFailureResponseModel {
  implicit val format = Json.format[RegistrationFailureResponseModel]
}

object RegistrationResponse extends Implicits {

  type L = RegistrationFailureResponseModel

  type R = RegistrationSuccessResponseModel

  type RegistrationResponse = Either[L, R]

  lazy val parseFailure: JsValue => L = (js: JsValue) => RegistrationFailureResponseModel(s"parse error: $js")


}