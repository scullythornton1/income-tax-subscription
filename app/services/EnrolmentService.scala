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

package services

import javax.inject.{Inject, Singleton}

import audit.{Logging, LoggingConfig}
import common.Constants.GovernmentGateway._
import connectors.{GGAdminConnector, GGConnector}
import models.ErrorModel
import models.gg.{EnrolRequest, KnownFactsRequest, KnownFactsSuccessResponseModel, TypeValuePair}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import utils.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentService @Inject()
(
  gGAdminConnector: GGAdminConnector,
  ggConnector: GGConnector,
  logging: Logging
) {

  def addKnownFacts(nino: String, mtditId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[Either[ErrorModel, KnownFactsSuccessResponseModel]] = {
    implicit val addKnownFactsLoggingConfig = EnrolmentService.addKnownFactsLoggingConfig
    logging.debug(s"Request: NINO=$nino, MTDITID=$mtditId")
    val knownFact1 = TypeValuePair(MTDITID, mtditId)
    val knownFact2 = TypeValuePair(NINO, nino)
    gGAdminConnector.addKnownFacts(KnownFactsRequest(List(knownFact1, knownFact2)))
  }

  def ggEnrol(nino: String, mtditId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    implicit val ggEnrolLoggingConfig = EnrolmentService.ggEnrolLoggingConfig
    logging.debug(s"Request: NINO=$nino, MTDITID=$mtditId")
    val enrolRequest = EnrolRequest(
      portalId = ggPortalId,
      serviceName = ggServiceName,
      friendlyName = ggFriendlyName,
      knownFacts = List(mtditId, nino)
    )

    ggConnector.enrol(enrolRequest)
  }

}

object EnrolmentService {
  val addKnownFactsLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "EnrolmentService.addKnownFacts")
  val ggEnrolLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "EnrolmentService.ggEnrol")
}