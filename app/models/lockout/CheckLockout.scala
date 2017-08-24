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

package models.lockout

import java.time.{Instant, LocalDateTime, ZoneId}

import play.api.libs.json._

case class CheckLockout(arn: String, expiryTimestamp: LocalDateTime)

object CheckLockout {

  implicit val temporalReads: Reads[LocalDateTime] = new Reads[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] = {
      (json \ "$date").validate[Long] map (millis =>
        LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()))
    }
  }

  implicit val temporalWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    override def writes(o: LocalDateTime): JsValue = Json.obj("$date" -> Instant.from(o).toEpochMilli)
  }

  val reader: Reads[CheckLockout] = new Reads[CheckLockout] {
    override def reads(json: JsValue): JsResult[CheckLockout] = {
      val arnv = (json \ arn).validate[String].get
      val exp = (json \ expiry).validate[LocalDateTime].get
      JsSuccess(CheckLockout(arnv, exp))
    }
  }

  val writer: Writes[CheckLockout] = new Writes[CheckLockout] {
    override def writes(o: CheckLockout): JsValue =
      Json.obj(arn -> JsString(o.arn), expiry -> Json.toJson(o.expiryTimestamp))
  }


  implicit def oFormat[T](format: Format[T]): OFormat[T] = {
    val oFormat: OFormat[T] = new OFormat[T]() {
      override def writes(o: T): JsObject = format.writes(o).as[JsObject]

      override def reads(json: JsValue): JsResult[T] = format.reads(json)
    }
    oFormat
  }

  implicit val formats: OFormat[CheckLockout] = Format[CheckLockout](reader, writer)


  val arn = "arn"
  val expiry = "expiryTimestamp"
}

