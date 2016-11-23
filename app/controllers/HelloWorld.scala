package controllers

import play.api.libs.json.Json
import services.{HelloWorldService, SandboxService, LiveService}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

trait HelloWorld extends BaseController with HeaderValidator {
  val service: HelloWorldService
  implicit val hc: HeaderCarrier


  final def world = validateAccept(acceptHeaderValidationRules).async {
    service.fetchWorld.map(as => Ok(Json.toJson(as))
    ) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }

  final def application = validateAccept(acceptHeaderValidationRules).async {
    service.fetchApplication.map(as => Ok(Json.toJson(as))
    ) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }

  final def user = validateAccept(acceptHeaderValidationRules).async {
    service.fetchUser.map(as => Ok(Json.toJson(as))
    ) recover {
      case _ => Status(ErrorInternalServerError.httpStatusCode)(Json.toJson(ErrorInternalServerError))
    }
  }
}

object SandboxController extends HelloWorld {
  override val service = SandboxService
  override implicit val hc: HeaderCarrier = HeaderCarrier()
}

object LiveController extends HelloWorld {
  override val service = LiveService
  override implicit val hc: HeaderCarrier = HeaderCarrier()
}
