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

package audit

import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Logger}
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{Audit, DataEvent}
import uk.gov.hmrc.play.http.HeaderCarrier

@Singleton
class Logging @Inject()(configuration: Configuration,
                        auditConnector: AuditConnector) {


  lazy val appName: String = configuration.getString("appName").getOrElse("APP NAME NOT SET")

  lazy val audit: Audit = new Audit(appName, auditConnector)

  private def sendDataEvent(transactionName: String,
                            path: String = "N/A",
                            tags: Map[String, String] = Map.empty[String, String],
                            detail: Map[String, String],
                            eventType: String)
                           (implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(
      DataEvent(
        appName,
        auditType = eventType,
        tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, path) ++ tags,
        detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails(detail.toSeq: _*)
      )
    )

  private def splunkToLogger(transactionName: String, detail: Map[String, String], eventType: String): String =
    s"${if (eventType.nonEmpty) eventType + "\n"}$transactionName\n$detail"

  private def splunkFunction(transactionName: String, detail: Map[String, String], eventType: String)(implicit hc: HeaderCarrier) = {
    Logger.debug(Logging.splunkString + splunkToLogger(transactionName, detail, eventType))
    sendDataEvent(
      transactionName = transactionName,
      detail = detail,
      eventType = eventType
    )
  }

  def audit(transactionName: String, detail: Map[String, String], eventType: String)(implicit hc: HeaderCarrier) = splunkFunction(transactionName, detail, eventType)

  @inline def trace(msg: String) = Logger.trace(msg)

  @inline def trace(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit = trace(splunkToLogger(transactionName, detail, eventType))

  @inline def debug(msg: String) = Logger.debug(msg)

  @inline def debug(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit = debug(splunkToLogger(transactionName, detail, eventType))

  @inline def info(msg: String) = Logger.info(msg)

  @inline def info(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit = info(splunkToLogger(transactionName, detail, eventType))

  @inline def warn(msg: String) = Logger.warn(msg)

  @inline def warn(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit = warn(splunkToLogger(transactionName, detail, eventType))

  @inline def err(msg: String) = Logger.error(msg)

  @inline def err(transactionName: String, detail: Map[String, String], eventType: String = ""): Unit = err(splunkToLogger(transactionName, detail, eventType))

}

object Logging {

  val auditRegistrationTxName: String = "Registration"

  val splunkString = "SPLUNK AUDIT:\n"

}
