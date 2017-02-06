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

package utils

import com.eclipsesource.schema.{SchemaType, SchemaValidator}
import play.api.libs.json.{JsValue, Json}

import scala.io.Source

trait Resources {

  lazy val defaultDataMap = Map(
    "$nino" -> TestConstants.testNino,
    "$countryCode" -> load(Resources.countryCodeSchema)
  )

  def load(path: String): String = Source.fromURL(getClass.getResource(path)).mkString

  def loadAndParseJsonWithDummyData(path: String): JsValue = Json.parse(loadAndReplace(path, defaultDataMap))


  def loadAndReplace(path: String, replaceMap: Map[String, String] = defaultDataMap): String = {
    val jsonString: String = load(path).mkString
    replaceMap.foldLeft(jsonString)((json, mapEntry) => json.replace(mapEntry._1, mapEntry._2))
  }

  def loadSchema(schemaPath: String): SchemaType = Json.fromJson[SchemaType](Json.parse(loadAndReplace(schemaPath))).get

  def validateJson(schemaPath: String, json: JsValue): Boolean = {
    val validator = SchemaValidator()
    val schema: SchemaType = loadSchema(schemaPath)
    validator.validate(schema, json).isSuccess
  }

}

object Resources extends Resources {
  lazy val registrationRequestSchema = "/schemas/registration_request.schema"
  lazy val registrationResponseSchema = "/schemas/registration_response.schema"
  lazy val countryCodeSchema = "/schemas/country_code"

}
