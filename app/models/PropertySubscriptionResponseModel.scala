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

import models.PropertySubscriptionResponse.{L, R}
import play.api.libs.json.{JsResult, JsValue, Json, Reads}
import play.api.mvc.MultipartFormData.ParseError

case class IncomeSourcesModel(incomeSourceId: String)
case class PropertySubscriptionResponseModel(safeId: String, mtditId: String, incomeSource: IncomeSourcesModel)
case class PropertySubscriptionFailureModel(code: String, reason: String)

object IncomeSourcesModel {
  implicit val formats = Json.format[IncomeSourcesModel]
}
object PropertySubscriptionResponseModel {
  implicit val formats = Json.format[PropertySubscriptionResponseModel]
}
object PropertySubscriptionFailureModel {
  implicit val formats = Json.format[PropertySubscriptionFailureModel]
}

object PropertySubscriptionResponse {

  type L = PropertySubscriptionFailureModel

  type R = PropertySubscriptionResponseModel

  type PropertySubscriptionResponse = Either[L, R]

  lazy val parseFailure: JsValue => L = (js: JsValue) => PropertySubscriptionFailureModel(code = "SERVER_ERROR", reason = s"parse error: $js")

  def parseAsLeft[L,R](jsValue: JsValue, parseError: L)(implicit lReader: Reads[L]): Either[L, R] = {
    val jsL : JsResult[L] = Json.fromJson[L](jsValue)
    jsL.fold(
      invalid => Left(parseError),
      valid => Left(valid)
    )
  }

  def parseAsRight[L, R](jsValue: JsValue, parseError: L)(implicit lReader: Reads[L], rReader: Reads[R]): Either[L, R] = {
    val jsR : JsResult[R] = Json.fromJson[R](jsValue)
    jsR.fold(
      invalid => Left(parseError),
      valid => Right(valid)
    )
  }
}
