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

import _root_.utils.JsonUtils._
import audit.Logging.{eventTypeRequest, eventTypeUnexpectedError}
import audit.{Logging, LoggingConfig}
import config.AppConfig
import models.gg._
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpReads, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class GGConnector @Inject()
(
  config: Configuration,
  httpPost: HttpPost,
  applicationConfig: AppConfig,
  logging: Logging
) extends ServicesConfig with RawResponseReads {

  lazy val governmentGatewayURL = applicationConfig.ggURL

  val enrolUrl: String = s"$governmentGatewayURL/enrol"

  def createHeaderCarrierPost(hc: HeaderCarrier): HeaderCarrier = hc.withExtraHeaders("Content-Type" -> "application/json")

  def enrol(enrolmentRequest: EnrolRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    import GGConnector._

    implicit lazy val loggingConfig = addEnrolLoggingConfig

    lazy val requestDetails: Map[String, String] = Map("enrolRequest" -> (enrolmentRequest: JsValue).toString)
    val updatedHc = createHeaderCarrierPost(hc)

    lazy val auditRequest = logging.auditFor(auditEnrolName, requestDetails)(updatedHc)
    auditRequest(eventTypeRequest)


    logging.debug(s"Request:\n$requestDetails")

    httpPost.POST[EnrolRequest, HttpResponse](enrolUrl, enrolmentRequest)(
      implicitly[Writes[EnrolRequest]], HttpReads.readRaw, createHeaderCarrierPost(hc)
    ).map { response =>

      lazy val audit = logging.auditFor(auditEnrolName, requestDetails + ("response" -> response.body))(updatedHc)

      response.status match {
        case OK => response
        case x =>
          logging.warn("GG enrol responded with a unexpected error")
          audit(eventTypeUnexpectedError)
          response
      }
    }

  }
}

object GGConnector {

  val auditEnrolName = "gg-enrol"

  import _root_.utils.Implicits.OptionUtl

  val addEnrolLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "GGConnector.enrol")


}