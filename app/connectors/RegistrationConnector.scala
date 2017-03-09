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
import connectors.utils.ConnectorUtils
import models.registration._
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationConnector @Inject()(config: Configuration,
                                      logging: Logging,
                                      httpPost: HttpPost,
                                      httpGet: HttpGet
                                     ) extends ServicesConfig with RawResponseReads {

  import Logging._

  lazy val urlHeaderEnvironment: String = config.getString("microservice.services.des.environment").fold("")(x => x)
  lazy val urlHeaderAuthorization: String = s"Bearer ${config.getString("microservice.services.des.authorization-token").fold("")(x => x)}"
  lazy val registrationServiceUrl: String = baseUrl("des")

  // DES API numbering [MTD API numbering]
  // API4 [API 9]
  val newRegistrationUrl: String => String = (nino: String) => s"$registrationServiceUrl/registration/individual/nino/$nino"

  // API 1(b) [API 1 (b)]
  val getRegistrationUrl: String => String = (nino: String) => s"$registrationServiceUrl/registration/details?nino=$nino"

  def createHeaderCarrierPost(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> urlHeaderEnvironment, "Content-Type" -> "application/json")
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def createHeaderCarrierGet(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> urlHeaderEnvironment)
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def register(nino: String, registration: RegistrationRequestModel)(implicit hc: HeaderCarrier): Future[NewRegistrationUtil.Response] = {
    import NewRegistrationUtil._
    import RegistrationConnector.auditRegisterName

    implicit val loggingConfig = RegistrationConnector.registerLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino, "requestJson" -> (registration: JsValue).toString)
    val updatedHc = createHeaderCarrierPost(hc)

    lazy val auditRequest = logging.auditFor(auditRegisterName, requestDetails)(updatedHc)
    auditRequest(eventTypeRequest)

    logging.debug(s"Request:\n$requestDetails")
    httpPost.POST[RegistrationRequestModel, HttpResponse](newRegistrationUrl(nino), registration)(
      implicitly[Writes[RegistrationRequestModel]], implicitly[HttpReads[HttpResponse]], updatedHc)
      .map { response =>

        lazy val audit = logging.auditFor(auditRegisterName, requestDetails + ("response" -> response.body))(updatedHc)
        val status = response.status

        status match {
          case OK => parseSuccess(response.body)
          case BAD_REQUEST =>
            logging.warn("Registration responded with a bad request error")
            audit(eventTypeBadRequest)
            parseFailure(BAD_REQUEST, response.body)
          case NOT_FOUND =>
            logging.warn("Registration responded with a not found error")
            audit(eventTypeNotFound)
            parseFailure(NOT_FOUND, response.body)
          case CONFLICT =>
            logging.warn("Registration responded with a conflict error")
            audit(eventTypeConflict)
            parseFailure(CONFLICT, response.body)
          case INTERNAL_SERVER_ERROR =>
            logging.warn("Registration responded with a internal server error")
            audit(eventTypeInternalServerError)
            parseFailure(INTERNAL_SERVER_ERROR, response.body)
          case SERVICE_UNAVAILABLE =>
            logging.warn("Registration responded with a service unavailable error")
            audit(eventTypeServerUnavailable)
            parseFailure(SERVICE_UNAVAILABLE, response.body)
          case x =>
            logging.warn("Registration responded with a unexpected error")
            audit(eventTypeUnexpectedError)
            parseFailure(x, response.body)
        }

      }
  }

  def getRegistration(nino: String)(implicit hc: HeaderCarrier): Future[GetRegistrationUtil.Response] = {
    import GetRegistrationUtil._
    import RegistrationConnector.auditGetRegistrationName

    implicit val loggingConfig = RegistrationConnector.getRegistrationLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino)
    val updatedHc = createHeaderCarrierPost(hc)

    lazy val auditRequest = logging.auditFor(auditGetRegistrationName, requestDetails)(updatedHc)
    auditRequest(eventTypeRequest)

    logging.debug(s"Request:\n$requestDetails")
    httpGet.GET[HttpResponse](getRegistrationUrl(nino))(implicitly[HttpReads[HttpResponse]], updatedHc)
      .map { response =>

        val status = response.status
        lazy val audit = logging.auditFor(auditGetRegistrationName, requestDetails + ("response" -> response.body))(updatedHc)

        status match {
          case OK => parseSuccess(response.body)
          case BAD_REQUEST =>
            audit(eventTypeBadRequest)
            parseFailure(BAD_REQUEST, response.body)
          case NOT_FOUND =>
            audit(eventTypeNotFound)
            parseFailure(NOT_FOUND, response.body)
          case INTERNAL_SERVER_ERROR =>
            audit(eventTypeInternalServerError)
            parseFailure(INTERNAL_SERVER_ERROR, response.body)
          case SERVICE_UNAVAILABLE =>
            audit(eventTypeServerUnavailable)
            parseFailure(SERVICE_UNAVAILABLE, response.body)
          case x =>
            audit(eventTypeUnexpectedError)
            parseFailure(x, response.body)
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

}


object NewRegistrationUtil extends ConnectorUtils[NewRegistrationFailureResponseModel, RegistrationSuccessResponseModel]

object GetRegistrationUtil extends ConnectorUtils[GetRegistrationFailureResponseModel, RegistrationSuccessResponseModel]
