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
import models.gg._
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.json.Writes
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
    httpPost.POST[EnrolRequest, HttpResponse](enrolUrl, enrolmentRequest)(
      implicitly[Writes[EnrolRequest]], HttpReads.readRaw, createHeaderCarrierPost(hc)
    ).map { response =>
      response.status match {
        case OK => response
        case x =>
          logging.warn(s"Unexpected failure status (${response.status})\n${response.body}")
          response
      }
    }

  }
}