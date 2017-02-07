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

import audit.Logging
import models.registration.{RegistrationRequestModel, RegistrationResponse}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Writes
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpReads, HttpResponse}
import utils.JsonUtil._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationConnector @Inject()(config: Configuration,
                                      logging: Logging,
                                      http: HttpPost
                                     ) extends ServicesConfig with RawResponseReads {

  import RegistrationResponse._

  lazy val urlHeaderEnvironment: String = config.getString("microservice.services.registration.environment").fold("")(x => x)
  lazy val urlHeaderAuthorization: String = s"Bearer ${config.getString("microservice.services.registration.authorization-token").fold("")(x => x)}"
  lazy val registrationServiceUrl: String = baseUrl("registration")

  val registrationUrl: String => String = (nino: String) => s"$registrationServiceUrl/registration/individual/NINO/$nino"

  def createHeaderCarrier(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> urlHeaderEnvironment, "Content-Type" -> "application/json").copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def register(nino: String, registration: RegistrationRequestModel)(implicit hc: HeaderCarrier): Future[RegistrationResponse] =
    http.POST[RegistrationRequestModel, HttpResponse](registrationUrl(nino), registration)(
      implicitly[Writes[RegistrationRequestModel]], implicitly[HttpReads[HttpResponse]], createHeaderCarrier(hc)).map { response =>
      lazy val defaultParseError = RegistrationResponse.parseFailure(response.body)
      response.status match {
        case OK => parse[L, R](response.body, defaultParseError)
        case BAD_REQUEST => parseAsLeft[L, R](response.body, defaultParseError)
        case NOT_FOUND => parseAsLeft[L, R](response.body, defaultParseError)
        case INTERNAL_SERVER_ERROR => parseAsLeft[L, R](response.body, defaultParseError)
        case SERVICE_UNAVAILABLE => parseAsLeft[L, R](response.body, defaultParseError)
        case _ => parseAsLeft[L, R](response.body, defaultParseError)
      }
    }

}
