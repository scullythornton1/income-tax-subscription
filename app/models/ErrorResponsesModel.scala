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

package models

import play.api.libs.json.{JsValue, Json, Writes}

sealed abstract class ErrorResponseModel(
                                     val httpStatusCode: Int,
                                     val errorCode: String,
                                     val message: String) {
  implicit val errorResponseWrites = new Writes[ErrorResponseModel] {
    def writes(e: ErrorResponseModel): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}

case object ErrorUnauthorized extends ErrorResponseModel(401, "UNAUTHORIZED", "Bearer token is missing or not authorized")

case object ErrorNotFound extends ErrorResponseModel(404, "NOT_FOUND", "Resource was not found")

case object ErrorGenericBadRequest extends ErrorResponseModel(400, "BAD_REQUEST", "Bad Request")

case object ErrorAcceptHeaderInvalid extends ErrorResponseModel(406, "ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

case object ErrorInternalServerError extends ErrorResponseModel(500, "INTERNAL_SERVER_ERROR", "Internal server error")
