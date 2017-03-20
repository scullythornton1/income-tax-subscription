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

package connectors

import javax.inject.Inject

import audit.Logging.{eventTypeBadRequest, eventTypeInternalServerError, eventTypeRequest, eventTypeUnexpectedError}
import audit.{Logging, LoggingConfig}
import common.Constants.GovernmentGateway
import config.AppConfig
import connectors.utils.ConnectorUtils
import models.gg.{KnownFactsFailureResponseModel, KnownFactsRequest, KnownFactsSuccessResponseModel}
import play.api.Configuration
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpReads, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class GGAdminConnector @Inject()(config: Configuration,
                                 applicationConfig: AppConfig,
                                 logging: Logging,
                                 httpPost: HttpPost
                                ) extends ServicesConfig with RawResponseReads {

  private lazy val ggAdminUrl: String = applicationConfig.ggAdminURL
  private lazy val serviceName: String = GovernmentGateway.ggServiceName

  val addKnownFactsUrl: String = s"$ggAdminUrl/government-gateway-admin/service/$serviceName/known-facts"

  def createHeaderCarrierPost(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Content-Type" -> "application/json")

  def addKnownFacts(knownFacts: KnownFactsRequest)(implicit hc: HeaderCarrier): Future[AddKnownFactsUtil.Response] = {
    import AddKnownFactsUtil._
    import GGAdminConnector.{addKnownFactsLoggingConfig, auditAddKnownFactsName}

    implicit lazy val loggingConfig = addKnownFactsLoggingConfig
    val updatedHc = createHeaderCarrierPost(hc)

    lazy val requestDetails: Map[String, String] = Map("knownFacts" -> (knownFacts: JsValue).toString)
    logging.debug(s"Request:\n$requestDetails")

    httpPost.POST[KnownFactsRequest, HttpResponse](addKnownFactsUrl, knownFacts)(
      implicitly[Writes[KnownFactsRequest]], implicitly[HttpReads[HttpResponse]], updatedHc).map { response =>

      lazy val audit = logging.auditFor(auditAddKnownFactsName, requestDetails + ("response" -> response.body))(updatedHc)
      val status = response.status
      status match {
        case OK => parseSuccess(response.body)
        case BAD_REQUEST =>
          logging.warn("GG admin responded with a bad request")
          audit(auditAddKnownFactsName + "-" + eventTypeBadRequest)
          parseFailure(BAD_REQUEST, response.body)
        case INTERNAL_SERVER_ERROR =>
          logging.warn("GG admin responded with an internal server error")
          audit(auditAddKnownFactsName + "-" + eventTypeInternalServerError)
          parseFailure(INTERNAL_SERVER_ERROR, response.body)
        case x =>
          logging.warn(s"GG admin responded with an unexpected status code ($x)")
          audit(auditAddKnownFactsName + "-" + eventTypeUnexpectedError)
          parseFailure(x, response.body)
      }
    }
  }

}

object GGAdminConnector {

  val auditAddKnownFactsName = "ggAdmin-addKnownFacts"

  import _root_.utils.Implicits.OptionUtl

  val addKnownFactsLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "GGAdminConnector.addKnownFacts")


}

object AddKnownFactsUtil extends ConnectorUtils[KnownFactsFailureResponseModel, KnownFactsSuccessResponseModel]
