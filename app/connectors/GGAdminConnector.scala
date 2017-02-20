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
import models.gg.{KnownFactsFailureResponseModel, KnownFactsRequest, KnownFactsSuccessResponseModel}
import play.api.Configuration
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class GGAdminConnector @Inject()(config: Configuration,
                                 applicationConfig: AppConfig,
                                 logging: Logging,
                                 httpPost: HttpPost
                                ) extends ServicesConfig with RawResponseReads {

  private lazy val ggAdminUrl: String = applicationConfig.ggAdminURL
  private lazy val serviceName: String = "ITSA"

  val addKnownFactsUrl: String = s"$ggAdminUrl/service/$serviceName/known-facts"

  def addKnownFacts(knownFacts: KnownFactsRequest)(implicit hc: HeaderCarrier): Future[AddKnownFactsUtil.Response] = {
    import AddKnownFactsUtil._
    httpPost.POST[KnownFactsRequest, HttpResponse](addKnownFactsUrl, knownFacts).map { response =>
      val status = response.status
      status match {
        case OK => parseSuccess(response.body)
        case BAD_REQUEST =>
          parseFailure(BAD_REQUEST, response.body)
        case INTERNAL_SERVER_ERROR =>
          parseFailure(INTERNAL_SERVER_ERROR, response.body)
        case x =>
          parseFailure(x, response.body)

      }
    }
  }

}

object AddKnownFactsUtil extends ConnectorUtils[KnownFactsFailureResponseModel, KnownFactsSuccessResponseModel]
