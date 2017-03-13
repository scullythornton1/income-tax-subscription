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

import play.api.libs.json.JsValue
import play.api.{Application, Configuration, Logger}
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{Audit, DataEvent}
import uk.gov.hmrc.play.http.HeaderCarrier

case class LoggingConfig(heading: String)

object LoggingConfig {

  implicit class LoggingConfigUtil(config: Option[LoggingConfig]) {
    def addHeading(message: String): String = config.fold(message)(x => x.heading + ": " + message)
  }

}

import utils.JsonUtils._

@Singleton
class Logging @Inject()(application: Application,
                        configuration: Configuration,
                        auditConnector: AuditConnector) {


  lazy val appName: String = configuration.getString("appName").getOrElse("APP NAME NOT SET")

  lazy val audit: Audit = new Audit(appName, auditConnector)

  private def sendDataEvent(transactionName: String,
                            path: String = "N/A",
                            tags: Map[String, String] = Map.empty[String, String],
                            detail: Map[String, String],
                            eventType: String)
                           (implicit hc: HeaderCarrier): Unit = {
    val packet = DataEvent(
      appName,
      auditType = transactionName + "-" + eventType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, path) ++ tags,
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails(detail.toSeq: _*)
    )
    val pjs = packet: JsValue
    audit.sendDataEvent(packet)
  }

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

  def audit(transactionName: String, detail: Map[String, String], eventType: String)(implicit hc: HeaderCarrier): Unit =
    splunkFunction(transactionName, detail, eventType)

  def auditFor(auditName: String)(implicit hc: HeaderCarrier): (Map[String, String], String) => Unit = audit(auditName, _, _)(hc)

  def auditFor(auditName: String, detail: Map[String, String])(implicit hc: HeaderCarrier): String => Unit = audit(auditName, detail, _)(hc)

  @inline def trace(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.trace(config.addHeading(msg))

  @inline def debug(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.debug(config.addHeading(msg))

  @inline def info(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.info(config.addHeading(msg))

  @inline def warn(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.warn(config.addHeading(msg))

  @inline def err(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.error(config.addHeading(msg))

}

object Logging {

  val auditRegistrationTxName: String = "Registration"

  val splunkString = "SPLUNK AUDIT:\n"

  final val eventTypeRequest: String = "Request"
  final val eventTypeSuccess: String = "Success"
  final val eventTypeFailure: String = "Failure"
  final val eventTypeBadRequest: String = "BadRequest"
  final val eventTypeConflict: String = "Conflict"
  final val eventTypeNotFound: String = "NotFound"
  final val eventTypeInternalServerError: String = "InternalServerError"
  final val eventTypeServerUnavailable: String = "ServerUnavailable"
  final val eventTypeUnexpectedError: String = "UnexpectedError"
}

