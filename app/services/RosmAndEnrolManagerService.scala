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

import audit.Logging
import config.AppConfig
import connectors.AuthenticatorConnector
import models.ErrorModel
import models.authenticator.{RefreshFailure, RefreshSuccessful}
import models.frontend._
import models.subscription.business.BusinessSubscriptionSuccessResponseModel
import models.subscription.property.PropertySubscriptionResponseModel
import play.api.http.Status._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import utils.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RosmAndEnrolManagerService @Inject()
(
  appConfig: AppConfig,
  logging: Logging,
  registrationService: RegistrationService,
  subscriptionService: SubscriptionService,
  enrolmentService: EnrolmentService,
  authenticatorConnector: AuthenticatorConnector
) {

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  val feRequestToAuditMap: FERequest => Map[String, String] = feRequest =>
    Map(
      "nino" -> feRequest.nino,
      "sourceOfIncome" -> feRequest.incomeSource.toString,
      "acccountingPeriodStartDate" -> feRequest.accountingPeriodStart.fold("-")(x => x.toDesDateFormat),
      "acccountingPeriodEndDate" -> feRequest.accountingPeriodEnd.fold("-")(x => x.toDesDateFormat),
      "tradingName" -> feRequest.tradingName.fold("-")(x => x),
      "cashOrAccruals" -> feRequest.cashOrAccruals.fold("-")(x => x.toLowerCase),
      "Authorization" -> urlHeaderAuthorization
    )

  val auditResponseMap: FESuccessResponse => Map[String, String] = response =>
    Map(
      "mtdItsaReferenceNumber" -> response.mtditId
    )

  def rosmAndEnrol(request: FERequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, FESuccessResponse]] = {
    logging.audit(Logging.AuditSubscribeRequest.transactionName, feRequestToAuditMap(request), Logging.AuditSubscribeRequest.auditType)(hc)
    orchestrateROSM(request).flatMap {
      case Right(rosmSuccess) =>
        orchestrateEnrolment(request.nino, rosmSuccess.mtditId).flatMap {
          case Right(enrolSuccess) =>
            authenticatorConnector.refreshProfile.map {
              case RefreshSuccessful =>
                logging.audit(Logging.AuditReferenceNumber.transactionName, auditResponseMap(rosmSuccess), Logging.AuditReferenceNumber.auditType)(hc)
                FESuccessResponse(rosmSuccess.mtditId)
              case RefreshFailure => ErrorModel(INTERNAL_SERVER_ERROR, "Authenticator Refresh Profile Failed")
            }
          case Left(enrolFailure) => Future.successful(enrolFailure)
        }
      case Left(rosmFailure) => Future.successful(rosmFailure)
    }
  }

  def orchestrateROSM(request: FERequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, FESuccessResponse]] = {
    registrationService.register(request.isAgent, request.nino) flatMap {
      case Right(success) => {
        for {
          businessResult <- businessSubscription(request)
          propertyResult <- propertySubscription(request)
        } yield (businessResult, propertyResult) match {
          case (Some(Left(err)), _) => err
          case (_, Some(Left(err))) => err
          case (Some(Right(x)), _) => FESuccessResponse(x.mtditId) // As long as there's no error reported then
          case (_, Some(Right(x))) => FESuccessResponse(x.mtditId) // We only need the response of one of the calls
          case (_, _) => ErrorModel(INTERNAL_SERVER_ERROR, "Unexpected Error") // this error is impossible but included for exhaustive match
        }
      }
      case Left(failure) => Future.successful(failure)
    }
  }

  def orchestrateEnrolment(nino: String, mtditId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext):
  Future[Either[ErrorModel, HttpResponse]] = {
    enrolmentService.addKnownFacts(nino, mtditId).flatMap {
      case Right(knownFactSuccess) => enrolmentService.ggEnrol(nino, mtditId).map(result =>
        result.status match {
          case OK => result
          case _ => ErrorModel(result.status, "Failed in call to Government Gateway Enrol")
        }
      )
      case Left(knownFactError) => Future.successful(knownFactError)
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
