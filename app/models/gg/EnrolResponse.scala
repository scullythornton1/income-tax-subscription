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

package models.gg

import models.ErrorResponsesModel
import play.api.libs.json.Json


case class EnrolResponse(serviceName: String,
                         state: String,
                         friendlyName: String,
                         identifiers: List[TypeValuePair])
case class EnrolFailureResponse(code: Option[String], reason: String) extends ErrorResponsesModel


object EnrolResponse {
  implicit val format = Json.format[EnrolResponse]
}
object EnrolFailureResponse {
  implicit val format = Json.format[EnrolResponse]
}

sealed trait EnrolResult

case object EnrolSuccess extends EnrolResult

case object EnrolFailure extends EnrolResult
