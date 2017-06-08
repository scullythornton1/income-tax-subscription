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

package models.frontend

import models.DateModel
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class FERequest
(
  nino: String,
  incomeSource: IncomeSourceType,
  isAgent: Boolean = false,
  arn: Option[String] = None,
  accountingPeriodStart: Option[DateModel] = None,
  accountingPeriodEnd: Option[DateModel] = None,
  tradingName: Option[String] = None,
  cashOrAccruals: Option[String] = None,
  // enrolUser must be set to true for individual and false for agent
  enrolUser: Boolean = true
)

object FERequest {
  val agentWithoutArnErrMsg = "The ARN must be supplied for an agent"
  val noneAgentWithArnErrMsg = "The ARN must not be supplied for a none agent"

  // custom reader to set enrolUser with the default value of true if it wasn't specified
  val reads: Reads[FERequest] = (
    (JsPath \ "nino").read[String] and
      (JsPath \ "incomeSource").read[IncomeSourceType] and
      (JsPath \ "isAgent").read[Boolean] and
      (JsPath \ "arn").readNullable[String] and
      (JsPath \ "accountingPeriodStart").readNullable[DateModel] and
      (JsPath \ "accountingPeriodEnd").readNullable[DateModel] and
      (JsPath \ "tradingName").readNullable[String] and
      (JsPath \ "cashOrAccruals").readNullable[String] and
      (JsPath \ "enrolUser").readNullable[Boolean].map(x => x.fold(true)(y => y))
    ) (FERequest.apply _)
    .filter(ValidationError(agentWithoutArnErrMsg)) {
      feRequest: FERequest =>
        if (feRequest.isAgent) feRequest.arn.nonEmpty
        else true
    }
    .filter(ValidationError(noneAgentWithArnErrMsg)) {
      feRequest: FERequest =>
        if (!feRequest.isAgent) feRequest.arn.isEmpty
        else true
    }

  val writes: OWrites[FERequest] = Json.writes[FERequest]

  implicit val format: OFormat[FERequest] = OFormat(reads, writes)
}
