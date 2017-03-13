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

import audit.Logging._
import audit.{Logging, LoggingConfig}
import config.AppConfig
import connectors.utils.ConnectorUtils
import models.subscription.business._
import models.subscription.property.{PropertySubscriptionFailureModel, PropertySubscriptionResponseModel}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject()
(
  applicationConfig: AppConfig,
  httpPost: HttpPost,
  logging: Logging
) extends ServicesConfig with RawResponseReads {

  val businessSubscribeUrl: String => String = nino => s"${applicationConfig.desURL}/income-tax-self-assessment/nino/$nino/business"
  val propertySubscribeUrl: String => String = nino => s"${applicationConfig.desURL}/income-tax-self-assessment/nino/$nino/properties"

  def createHeaderCarrierPost(hc: HeaderCarrier): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(s"Bearer ${applicationConfig.desToken}")))
      .withExtraHeaders("Environment" -> applicationConfig.desEnvironment, "Content-Type" -> "application/json")

  def createHeaderCarrierPostEmpty(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.copy(authorization = Some(Authorization(s"Bearer ${applicationConfig.desToken}")))
      .withExtraHeaders("Environment" -> applicationConfig.desEnvironment)

  def businessSubscribe(nino: String, businessSubscriptionPayload: BusinessSubscriptionRequestModel)
                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[BusinessConnectorUtil.Response] = {
    import BusinessConnectorUtil._
    import SubscriptionConnector._
    implicit val loggingConfig = SubscriptionConnector.businessSubscribeLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino, "subscribe" -> (businessSubscriptionPayload: JsValue).toString)
    val updatedHc = createHeaderCarrierPost(hc)

    lazy val auditRequest = logging.auditFor(auditBusinessSubscribeName, requestDetails)(updatedHc)
    auditRequest(eventTypeRequest)

    logging.debug(s"Request:\n$requestDetails")
    httpPost.POST[BusinessSubscriptionRequestModel, HttpResponse](businessSubscribeUrl(nino), businessSubscriptionPayload)(
      implicitly[Writes[BusinessSubscriptionRequestModel]], HttpReads.readRaw, createHeaderCarrierPost(hc)
    ).map { response =>

      lazy val audit = logging.auditFor(auditBusinessSubscribeName, requestDetails + ("response" -> response.body))(updatedHc)

      response.status match {
        case OK => parseSuccess(response.body)
        case x =>
          logging.warn("Business subscription responded with a unexpected error")
          audit(eventTypeUnexpectedError)
          parseFailure(x, response.body)
      }
    }
  }

  def propertySubscribe(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PropertyConnectorUtil.Response] = {
    import PropertyConnectorUtil._
    import SubscriptionConnector._
    implicit val loggingConfig = SubscriptionConnector.propertySubscribeLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino)
    val updatedHc = createHeaderCarrierPostEmpty(hc)

    lazy val auditRequest = logging.auditFor(auditPropertySubscribeName, requestDetails)(updatedHc)
    auditRequest(eventTypeRequest)

    logging.debug(s"Request:\n$requestDetails")
    httpPost.POSTEmpty[HttpResponse](propertySubscribeUrl(nino))(HttpReads.readRaw, createHeaderCarrierPostEmpty(hc)).map {
      response =>

        lazy val audit = logging.auditFor(auditPropertySubscribeName, requestDetails + ("response" -> response.body))(updatedHc)

        response.status match {
          case OK => parseSuccess(response.body)
          case x =>
            logging.warn("Property subscription responded with a unexpected error")
            audit(eventTypeUnexpectedError)
            parseFailure(x, response.body)
        }
    }
  }
}

object SubscriptionConnector {

  import _root_.utils.Implicits.OptionUtl

  val auditBusinessSubscribeName = "business-subscribe-api-10"
  val businessSubscribeLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionConnector.businessSubscribe")

  val auditPropertySubscribeName = "property-subscribe-api-35"
  val propertySubscribeLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionConnector.propertySubscribe")
}

object PropertyConnectorUtil extends ConnectorUtils[PropertySubscriptionFailureModel, PropertySubscriptionResponseModel]

object BusinessConnectorUtil extends ConnectorUtils[BusinessSubscriptionErrorResponseModel, BusinessSubscriptionSuccessResponseModel]
