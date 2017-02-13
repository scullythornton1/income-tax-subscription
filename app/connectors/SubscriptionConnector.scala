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
import config.AppConfig
import connectors.utils.ConnectorUtils
import models.{PropertySubscriptionFailureModel, PropertySubscriptionResponseModel}
import models.subscription.business._
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Writes
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject()
(
  config: Configuration,
  httpPost: HttpPost,
  applicationConfig: AppConfig
) extends ServicesConfig with RawResponseReads {

  lazy val desServiceUrl = applicationConfig.desURL
  lazy val urlHeaderEnvironment = applicationConfig.desEnvironment
  lazy val urlHeaderAuthorization = applicationConfig.desToken

  val businessSubscribeUrl: String => String = nino => s"$desServiceUrl/income-tax-self-assessment/nino/$nino/business"
  val propertySubscribeUrl: String => String = nino => s"$desServiceUrl/income-tax-self-assessment/nino/$nino/properties"

  def createHeaderCarrierPost(hc: HeaderCarrier): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(s"Bearer $urlHeaderAuthorization")))
      .withExtraHeaders("Environment" -> urlHeaderEnvironment, "Content-Type" -> "application/json")

  def businessSubscribe(nino: String, businessSubscriptionPayload: BusinessSubscriptionRequestModel)
                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[BusinessConnectorUtil.Response] = {
    import BusinessConnectorUtil._
    httpPost.POST[BusinessSubscriptionRequestModel, HttpResponse](businessSubscribeUrl(nino), businessSubscriptionPayload)(
      implicitly[Writes[BusinessSubscriptionRequestModel]], HttpReads.readRaw, createHeaderCarrierPost(hc)
    ).map { response =>
      response.status match {
        case OK => parseSuccess(response.body)
        case x => parseFailure(x, response.body)
      }
    }
  }

  def propertySubscribe(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PropertyConnectorUtil.Response] = {
    import PropertyConnectorUtil._
    httpPost.POSTEmpty[HttpResponse](propertySubscribeUrl(nino))(HttpReads.readRaw, createHeaderCarrierPost(hc)).map {
      response => response.status match {
        case OK => parseSuccess(response.body)
        case x => parseFailure(x, response.body)
      }
    }
  }
}

object PropertyConnectorUtil extends ConnectorUtils[PropertySubscriptionFailureModel, PropertySubscriptionResponseModel]
object BusinessConnectorUtil extends ConnectorUtils[BusinessSubscriptionErrorResponseModel, BusinessSubscriptionSuccessResponseModel]
