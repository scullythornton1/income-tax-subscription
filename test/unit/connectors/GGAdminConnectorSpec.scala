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

package unit.connectors

import models.ErrorModel
import models.gg.KnownFactsSuccessResponseModel
import models.registration._
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.connectors.mocks.MockGGAdminConnector
import utils.JsonUtils._
import utils.TestConstants.GG._
import KnownFactsResponse._
import org.scalatest.Matchers._

class GGAdminConnectorSpec extends MockGGAdminConnector{

  implicit val hc = HeaderCarrier()

  "GGAdminConnector.addKnownFacts" should {

    "Post to the correct url" in {
      TestGGAdminConnector.ggAdminUrl should endWith("service/ITSA/known-facts")
    }

    def result = await(TestGGAdminConnector.addKnownFacts(knowFactsRequest))

    "parse and return the success response correctly" in {
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      result shouldBe Right(KnownFactsSuccessResponseModel(1))
    }

    "parse and return the Bad request response correctly" in {
      mockAddKnownFacts(knowFactsRequest)(SERVICE_DOES_NOT_EXISTS)
      result shouldBe Left(SERVICE_DOES_NOT_EXISTS_MODEL)
    }

  }


}
