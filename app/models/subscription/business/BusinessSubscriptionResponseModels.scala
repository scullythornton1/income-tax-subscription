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

package models.subscription.business

import models.ErrorResponsesModel
import models.subscription.IncomeSourceModel
import play.api.libs.json._


case class BusinessSubscriptionSuccessResponseModel(safeId: String, mtditId: String, incomeSources: List[IncomeSourceModel])

case class BusinessSubscriptionErrorResponseModel(code: Option[String], reason: String) extends ErrorResponsesModel

object BusinessSubscriptionSuccessResponseModel {
  implicit val format = Json.format[BusinessSubscriptionSuccessResponseModel]
}

object BusinessSubscriptionErrorResponseModel {
  implicit val format = Json.format[BusinessSubscriptionErrorResponseModel]
}