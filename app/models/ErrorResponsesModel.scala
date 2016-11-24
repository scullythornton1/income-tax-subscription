package models

import play.api.libs.json.{JsValue, Json, Writes}

sealed abstract class ErrorResponseModel(
                                     val httpStatusCode: Int,
                                     val errorCode: String,
                                     val message: String) {
  implicit val errorResponseWrites = new Writes[ErrorResponseModel] {
    def writes(e: ErrorResponseModel): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}

case object ErrorUnauthorized extends ErrorResponseModel(401, "UNAUTHORIZED", "Bearer token is missing or not authorized")

case object ErrorNotFound extends ErrorResponseModel(404, "NOT_FOUND", "Resource was not found")

case object ErrorGenericBadRequest extends ErrorResponseModel(400, "BAD_REQUEST", "Bad Request")

case object ErrorAcceptHeaderInvalid extends ErrorResponseModel(406, "ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

case object ErrorInternalServerError extends ErrorResponseModel(500, "INTERNAL_SERVER_ERROR", "Internal server error")



