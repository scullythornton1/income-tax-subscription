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

import audit.{Logging, LoggingConfig}
import config.AppConfig
import connectors.utils.ConnectorUtils
import models.ErrorModel
import models.registration.{GetBusinessDetailsFailureResponseModel, GetBusinessDetailsSuccessResponseModel}
import play.api.http.Status._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BusinessDetailsConnector @Inject()(appConfig: AppConfig,
                                         logging: Logging,
                                         httpGet: HttpGet
                                        ) extends ServicesConfig with RawResponseReads {

  import Logging._

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  // API 5
  def getBusinessDetailsUrl(nino: String): String = s"${appConfig.desURL}${BusinessDetailsConnector.getBusinessDetailsUri(nino)}"

  def createHeaderCarrierGet(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> appConfig.desEnvironment)
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def getBusinessDetails(nino: String)(implicit hc: HeaderCarrier): Future[GetBusinessDetailsUtil.Response] = {
    import BusinessDetailsConnector.auditGetBusinessDetails
    import GetBusinessDetailsUtil._
    implicit val loggingConfig = RegistrationConnector.getRegistrationLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino)
    val updatedHc = createHeaderCarrierGet(hc)
    lazy val auditRequest = logging.auditFor(auditGetBusinessDetails, requestDetails)(updatedHc)
    logging.debug(s"Request:\n$requestDetails\n\nRequest Headers:\n$updatedHc")

    httpGet.GET[HttpResponse](getBusinessDetailsUrl(nino))(implicitly[HttpReads[HttpResponse]], updatedHc)
      .map { response =>
        val status = response.status
        lazy val audit = logging.auditFor(auditGetBusinessDetails, requestDetails + ("response" -> response.body))(updatedHc)
        status match {
          case OK =>
            logging.info("Get Business Details responded with OK")
            parseSuccess(response.body)
          case BAD_REQUEST =>
            logging.warn("Get Business Details responded with a bad request error")
            auditRequest(eventTypeRequest)
            audit(auditGetBusinessDetails + "-" + eventTypeBadRequest)
            parseFailure(BAD_REQUEST, response.body)
          case NOT_FOUND =>
            val notFound = parseFailure(NOT_FOUND, response.body)
            // only audit if it's an unexpected error, since NOT_FOUND is something we expect on most occasions
            notFound match {
              case Left(ErrorModel(NOT_FOUND, Some("NOT_FOUND_NINO"), _)) => // expected case, do not audit
              case _ =>
                logging.warn("Get Business Details responded with a not found error")
                auditRequest(eventTypeRequest)
                audit(auditGetBusinessDetails + "-" + eventTypeNotFound)
            }
            notFound
          case INTERNAL_SERVER_ERROR =>
            logging.warn("Get Business Details responded with an internal server error")
            auditRequest(eventTypeRequest)
            audit(auditGetBusinessDetails + "-" + eventTypeInternalServerError)
            parseFailure(INTERNAL_SERVER_ERROR, response.body)
          case SERVICE_UNAVAILABLE =>
            logging.warn("Get Business Details responded with a service unavailable error")
            auditRequest(eventTypeRequest)
            audit(auditGetBusinessDetails + "-" + eventTypeServerUnavailable)
            parseFailure(SERVICE_UNAVAILABLE, response.body)
          case x =>
            logging.warn(s"Get Business Details responded with an unexpected error: status=$x")
            auditRequest(eventTypeRequest)
            audit(auditGetBusinessDetails + "-" + eventTypeUnexpectedError)
            parseFailure(x, response.body)
        }
      }
  }
}

object BusinessDetailsConnector {

  val auditGetBusinessDetails = "getBusinessDetails api-5"

  import _root_.utils.Implicits.OptionUtl

  val getRegistrationLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "BusinessDetailsConnector.getBusinessDetails")

  def getBusinessDetailsUri(nino: String): String = s"/registration/business-details/nino/$nino"
}

object GetBusinessDetailsUtil extends ConnectorUtils[GetBusinessDetailsFailureResponseModel, GetBusinessDetailsSuccessResponseModel]
