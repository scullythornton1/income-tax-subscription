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

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import connectors.utils.ConnectorUtils
import models.{PropertySubscriptionFailureModel, PropertySubscriptionResponseModel}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization

import scala.concurrent.{ExecutionContext, Future}
import _root_.utils.JsonUtils._

@Singleton
class SubscriptionETMPConnector @Inject()(http: HttpPost, applicationConfig: AppConfig)
  extends ServicesConfig with RawResponseReads {

  lazy val serviceUrl = applicationConfig.desURL
  lazy val environment = applicationConfig.desEnvironment
  lazy val token = applicationConfig.desToken

  def subscribePropertyEtmp(nino: String)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SubscriptionETMPConnector.Response] = {
    import SubscriptionETMPConnector._
    val requestUrl = s"$serviceUrl/income-tax-self-assessment/nino/$nino/properties"
    val desHeaders = hc.copy(authorization = Some(Authorization(s"Bearer $token"))).withExtraHeaders("Environment" -> environment)
    val request = http.POSTEmpty[HttpResponse](requestUrl)(HttpReads.readRaw, desHeaders)
    request.map {

      response =>
        lazy val jsValue = Json.parse(response.body)

        response.status match {
          case OK => parseSuccess(jsValue)
          case x => parseFailure(x, jsValue)
        }
    }
  }
}

object SubscriptionETMPConnector extends ConnectorUtils[PropertySubscriptionFailureModel, PropertySubscriptionResponseModel]
