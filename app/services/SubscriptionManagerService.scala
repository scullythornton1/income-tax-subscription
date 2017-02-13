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

package services

import javax.inject.{Inject, Singleton}

import models.ErrorModel
import models.frontend.{Both, Business, FERequest, FESuccessResponse, Property}
import models.subscription.business.BusinessSubscriptionSuccessResponseModel
import models.subscription.property.PropertySubscriptionResponseModel
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionManagerService @Inject()
(
  registrationService: RegistrationService,
  subscriptionService: SubscriptionService
) {

  //TODO add the calls and routing to the other services
  def orchestrateSubscription(request: FERequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, FESuccessResponse]] = {

    registrationService.register(request.isAgent, request.nino) flatMap {
      case Right(success) => {
        for {
          businessResult <- businessSubscription(request)
          propertyResult <- propertySubscription(request)
        } yield (businessResult, propertyResult) match {
          case (Some(Left(err)), _) => err
          case (_, Some(Left(err))) => err
          case (Some(Right(x)), _) => FESuccessResponse(x.mtditId)
          case (_, Some(Right(x))) => FESuccessResponse(x.mtditId)
        }
      }
      case Left(failure) => {
        println("")
        println("")
        println("")
        println("")
        println("")
        println(s"!!!!!! failed Registration call !!!!!!!!!!!!!")
        println("")
        println("")
        println("")
        println("")
        println("")
        Future.successful(failure)
      }
    }

  }

  def businessSubscription(request: FERequest)(implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[Option[Either[ErrorModel, BusinessSubscriptionSuccessResponseModel]]] = {
    request.incomeSource match {
      case Both | Business => subscriptionService.businessSubscribe(request) map {
        case Right(success) => Some(success)
        case Left(failure) => Some(failure)
      }
      case _ => None
    }
  }

  def propertySubscription(request: FERequest)(implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[Option[Either[ErrorModel, PropertySubscriptionResponseModel]]] = {
    request.incomeSource match {
      case Both | Property => subscriptionService.propertySubscribe(request) map {
        case Right(success) => Some(success)
        case Left(failure) => Some(failure)
      }
      case _ => None
    }
  }
}
