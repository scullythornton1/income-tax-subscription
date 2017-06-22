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

import audit.Logging.{eventTypeBadRequest, eventTypeInternalServerError, eventTypeUnexpectedError}
import audit.{Logging, LoggingConfig}
import common.Constants.GovernmentGateway
import config.AppConfig
import connectors.GGAdminConnector._
import connectors.utils.ConnectorUtils
import models.ErrorModel
import models.gg.{KnownFactsFailureResponseModel, KnownFactsRequest, KnownFactsSuccessResponseModel}
import play.api.Configuration
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpReads, HttpResponse}

import scala.annotation.switch
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class GGAdminConnector @Inject()(config: Configuration,
                                 applicationConfig: AppConfig,
                                 logging: Logging,
                                 httpPost: HttpPost
                                ) extends ServicesConfig with RawResponseReads {

  private lazy val ggAdminUrl: String = applicationConfig.ggAdminURL

  val addKnownFactsUrl: String = ggAdminUrl + addKnownFactsUri

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

      response.status match {
        case OK =>
          logging.info("GG admin responded with OK")
          parseSuccess(response.body)
        case status =>
          @switch
          val suffix = status match {
            case BAD_REQUEST => eventTypeBadRequest
            case INTERNAL_SERVER_ERROR => eventTypeInternalServerError
            case _ => eventTypeUnexpectedError
          }
          logging.audit(
            transactionName = auditAddKnownFactsName,
            detail = requestDetails + ("response" -> response.body),
            auditType = auditAddKnownFactsName + "-" + suffix
          )(updatedHc)

          val parseResponse@Left(ErrorModel(_, optCode, message)) = parseFailure(status, response.body)
          val code: String = optCode.getOrElse("N/A")
          logging.warn(s"GG admin responded with an error, status=$status code=$code message=$message")

          parseResponse
      }
    }
  }

}

object GGAdminConnector {
  private lazy val serviceName: String = GovernmentGateway.ggServiceName

  val auditAddKnownFactsName = "ggAdmin-addKnownFacts"

  import _root_.utils.Implicits.OptionUtl

  val addKnownFactsLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "GGAdminConnector.addKnownFacts")

  val addKnownFactsUri: String = s"/government-gateway-admin/service/$serviceName/known-facts"
}

object AddKnownFactsUtil extends ConnectorUtils[KnownFactsFailureResponseModel, KnownFactsSuccessResponseModel]
