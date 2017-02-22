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
import models.authenticator.{RefreshFailure, RefreshProfileResult, RefreshSuccessful}
import play.api.Configuration
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class AuthenticatorConnector @Inject()(configuration: Configuration,
                                       appConfig: AppConfig,
                                       logging: Logging,
                                       httpPost: HttpPost
                                      ) extends RawResponseReads {

  lazy val refreshProfileURI = s"${appConfig.authenticatorURL}/authenticator/refresh-profile"

  def refreshProfile(implicit hc: HeaderCarrier): Future[RefreshProfileResult] =
    httpPost.POSTEmpty[HttpResponse](refreshProfileURI).map {
      response =>
        implicit lazy val loggingConfig = AuthenticatorConnector.refreshProfileLoggingConfig

        response.status match {
          case 204 => RefreshSuccessful
          case x =>
            logging.warn(s"Unexpected failure status (${response.status})\n${response.body}")
            RefreshFailure
        }
    }

}

object AuthenticatorConnector {

  import _root_.utils.Implicits.OptionUtl

  val refreshProfileLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "RegistrationConnector.register")

}
