package controllers

import common.validation.HeaderValidator
import models.ErrorInternalServerError
import play.api.libs.json.Json
import services.{HelloWorldService, LiveHelloWorldService, SandboxHelloWorldService}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController
import scala.concurrent.ExecutionContext.Implicits.global

trait HelloWorldController extends BaseController with HeaderValidator {
  val service: HelloWorldService
  implicit val hc: HeaderCarrier

  val world = validateAccept(acceptHeaderValidationRules).async {
    service.fetchWorld.map(as => Ok(Json.toJson(as))
    ) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }

  val application = validateAccept(acceptHeaderValidationRules).async {
    service.fetchApplication.map(as => Ok(Json.toJson(as))
    ) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }

  val user = validateAccept(acceptHeaderValidationRules).async {
    service.fetchUser.map(as => Ok(Json.toJson(as))
    ) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }
}

object SandboxHelloWorldController extends HelloWorldController {
  override val service = SandboxHelloWorldService
  override implicit val hc: HeaderCarrier = HeaderCarrier()
}

object LiveHelloWorldController extends HelloWorldController {
  override val service = LiveHelloWorldService
  override implicit val hc: HeaderCarrier = HeaderCarrier()
}
