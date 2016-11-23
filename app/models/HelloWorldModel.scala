package models

import play.api.libs.json.Json

case class HelloWorldModel(message: String)

object HelloWorldModel {
  implicit val format = Json.format[HelloWorldModel]
}