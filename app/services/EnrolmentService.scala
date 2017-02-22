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

import connectors.GGAdminConnector
import models.ErrorModel
import models.gg.{KnownFactsRequest, KnownFactsSuccessResponseModel, TypeValuePair}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class EnrolmentService @Inject()(gGAdminConnector: GGAdminConnector) {

  import EnrolmentService._

  def addKnownFacts(nino: String, mtditId: String)(implicit hc: HeaderCarrier): Future[Either[ErrorModel, KnownFactsSuccessResponseModel]] = {
    val knownFact1 = TypeValuePair(MTDITID, mtditId)
    val knownFact2 = TypeValuePair(NINO, nino)
    gGAdminConnector.addKnownFacts(KnownFactsRequest(List(knownFact1, knownFact2)))
  }

}

object EnrolmentService {

  val MTDITID = "MTDITID"
  val NINO = "NINO"

}
