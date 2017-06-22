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
import models.registration._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization

import scala.annotation.switch
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationConnector @Inject()(appConfig: AppConfig,
                                      logging: Logging,
                                      httpPost: HttpPost,
                                      httpGet: HttpGet
                                     ) extends ServicesConfig with RawResponseReads {

  import Logging._

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  // DES API numbering [MTD API numbering]
  // API4 [API 9]
  def newRegistrationUrl(nino: String): String = s"${appConfig.desURL}${RegistrationConnector.newRegistrationUri(nino)}"

  // API 1(b) [API 1 (b)]
  def getRegistrationUrl(nino: String): String = s"${appConfig.desURL}${RegistrationConnector.getRegistrationUri(nino)}"

  def createHeaderCarrierPost(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> appConfig.desEnvironment, "Content-Type" -> "application/json")
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def createHeaderCarrierGet(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> appConfig.desEnvironment)
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def register(nino: String, registration: RegistrationRequestModel)(implicit hc: HeaderCarrier): Future[NewRegistrationUtil.Response] = {
    import NewRegistrationUtil._
    import RegistrationConnector.auditRegisterName

    implicit val loggingConfig = RegistrationConnector.registerLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino, "requestJson" -> (registration: JsValue).toString)
    val updatedHc = createHeaderCarrierPost(hc)

    logging.debug(s"Request:\n$requestDetails\n\nRequest Headers:\n$updatedHc")
    httpPost.POST[RegistrationRequestModel, HttpResponse](newRegistrationUrl(nino), registration)(
      implicitly[Writes[RegistrationRequestModel]], implicitly[HttpReads[HttpResponse]], updatedHc)
      .map { response =>

        lazy val audit = logging.auditFor(auditRegisterName, requestDetails + ("response" -> response.body))(updatedHc)
        val status = response.status

        status match {
          case OK =>
            logging.info("Registration responded with OK")
            parseSuccess(response.body)
          case BAD_REQUEST =>
            logging.warn("Registration responded with a bad request error")
            audit(auditRegisterName + "-" + eventTypeBadRequest)
            parseFailure(BAD_REQUEST, response.body)
          case NOT_FOUND =>
            logging.warn("Registration responded with a not found error")
            audit(auditRegisterName + "-" + eventTypeNotFound)
            parseFailure(NOT_FOUND, response.body)
          case CONFLICT =>
            logging.warn("Registration responded with a conflict error")
            audit(auditRegisterName + "-" + eventTypeConflict)
            parseFailure(CONFLICT, response.body)
          case INTERNAL_SERVER_ERROR =>
            logging.warn("Registration responded with an internal server error")
            audit(auditRegisterName + "-" + eventTypeInternalServerError)
            parseFailure(INTERNAL_SERVER_ERROR, response.body)
          case SERVICE_UNAVAILABLE =>
            logging.warn("Registration responded with a service unavailable error")
            audit(auditRegisterName + "-" + eventTypeServerUnavailable)
            parseFailure(SERVICE_UNAVAILABLE, response.body)
          case x =>
            logging.warn(s"Registration responded with an unexpected error: status=$x")
            audit(auditRegisterName + "-" + eventTypeUnexpectedError)
            parseFailure(x, response.body)
        }

      }
  }

  def getRegistration(nino: String)(implicit hc: HeaderCarrier): Future[GetRegistrationUtil.Response] = {
    import GetRegistrationUtil._
    import RegistrationConnector.auditGetRegistrationName

    implicit val loggingConfig = RegistrationConnector.getRegistrationLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino)
    val updatedHc = createHeaderCarrierGet(hc)

    lazy val auditRequest = logging.auditFor(auditGetRegistrationName, requestDetails)(updatedHc)
    auditRequest(eventTypeRequest)

    logging.debug(s"Request:\n$requestDetails\n\nRequest Headers:\n$updatedHc")
    httpGet.GET[HttpResponse](getRegistrationUrl(nino))(implicitly[HttpReads[HttpResponse]], updatedHc)
      .map { response =>
        response.status match {
          case OK =>
            logging.info("Get Registration responded with an OK")
            parseSuccess(response.body)
          case status =>
            @switch
            val suffix = status match {
              case BAD_REQUEST => eventTypeBadRequest
              case NOT_FOUND => eventTypeNotFound
              case INTERNAL_SERVER_ERROR => eventTypeInternalServerError
              case SERVICE_UNAVAILABLE => eventTypeServerUnavailable
              case _ => eventTypeUnexpectedError
            }
            logging.audit(
              transactionName = auditGetRegistrationName,
              detail = requestDetails + ("response" -> response.body),
              auditType = auditGetRegistrationName + "-" + suffix
            )(updatedHc)

            val parseResponse@ErrorModel(_, optCode, message) = parseFailure(status, response.body)
            val code: String = optCode.getOrElse("N/A")
            logging.warn(s"Get Registration responded with an error, status=$status code=$code message=$message")

            parseResponse
        }
      }
  }

}

object RegistrationConnector {

  val auditRegisterName = "register-api-4"

  val auditGetRegistrationName = "getRegistration-api-1(b)"

  import _root_.utils.Implicits.OptionUtl

  val registerLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "RegistrationConnector.register")

  val getRegistrationLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "RegistrationConnector.getRegistration")

  // DES API numbering [MTD API numbering]
  // API4 [API 9]
  def newRegistrationUri(nino: String): String = s"/registration/individual/nino/$nino"

  // API 1(b) [API 1 (b)]
  def getRegistrationUri(nino: String): String = s"/registration/details?nino=$nino"
}


object NewRegistrationUtil extends ConnectorUtils[NewRegistrationFailureResponseModel, RegistrationSuccessResponseModel]

object GetRegistrationUtil extends ConnectorUtils[GetRegistrationFailureResponseModel, RegistrationSuccessResponseModel]
