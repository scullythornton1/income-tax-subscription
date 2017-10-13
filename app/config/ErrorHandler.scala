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

package config

import javax.inject.Inject

import play.api.Configuration
import play.api.mvc.{RequestHeader, Result, Results}
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.http.JsonErrorHandler

import scala.concurrent.Future

class ErrorHandler @Inject()(configuration: Configuration, auditConnector: AuditConnector)
  extends JsonErrorHandler(configuration, auditConnector) {
  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    ex match {
      case authEx: AuthorisationException => Future.successful(Results.Unauthorized(authEx.reason))
      case _ => super.onServerError(request, ex)
    }
  }
}
