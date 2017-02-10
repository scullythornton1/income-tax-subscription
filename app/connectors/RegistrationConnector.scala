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
import connectors.utils.ConnectorUtils
import models.registration._
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Writes
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


  lazy val urlHeaderEnvironment: String = config.getString("microservice.services.des.environment").fold("")(x => x)
  lazy val urlHeaderAuthorization: String = s"Bearer ${config.getString("microservice.services.des.authorization-token").fold("")(x => x)}"
  lazy val registrationServiceUrl: String = baseUrl("des")

  val newRegistrationUrl: String => String = (nino: String) => s"$registrationServiceUrl/registration/individual/NINO/$nino"

  val getRegistrationUrl: String => String = (nino: String) => s"$registrationServiceUrl/registration/details?nino=$nino"

  def createHeaderCarrierPost(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> urlHeaderEnvironment, "Content-Type" -> "application/json")
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def createHeaderCarrierGet(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> urlHeaderEnvironment)
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))


  def register(nino: String, registration: RegistrationRequestModel)(implicit hc: HeaderCarrier): Future[NewRegistrationUtil.Response] = {
    import NewRegistrationUtil._
    httpPost.POST[RegistrationRequestModel, HttpResponse](newRegistrationUrl(nino), registration)(
      implicitly[Writes[RegistrationRequestModel]], implicitly[HttpReads[HttpResponse]], createHeaderCarrierPost(hc)).map { response =>
      val status = response.status
      status match {
        case OK => parseSuccess(response.body)
        case x => parseFailure(x, response.body)
      }
    }
  }

  def getRegistration(nino: String)(implicit hc: HeaderCarrier): Future[GetRegistrationUtil.Response] = {
    import GetRegistrationUtil._
    httpGet.GET[HttpResponse](getRegistrationUrl(nino))(implicitly[HttpReads[HttpResponse]], createHeaderCarrierGet(hc)).map { response =>
      val status = response.status
      status match {
        case OK => parseSuccess(response.body)
        case x => parseFailure(x, response.body)
      }
    }
  }

}


object NewRegistrationUtil extends ConnectorUtils[NewRegistrationFailureResponseModel, RegistrationSuccessResponseModel]

object GetRegistrationUtil extends ConnectorUtils[GetRegistrationFailureResponseModel, RegistrationSuccessResponseModel]
