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

package controllers.digitalcontact

import helpers.ComponentSpecBase
import helpers.servicemocks.AuthStub
import models.digitalcontact.PaperlessPreferenceKey
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import repositories.digitalcontact.PaperlessPreferenceMongoRepository
import helpers.IntegrationTestConstants._
import scala.concurrent.ExecutionContext.Implicits.global

class PaperlessPreferenceControllerISpec extends ComponentSpecBase with BeforeAndAfterEach {
  val paperlessPreferenceRepository = app.injector.instanceOf[PaperlessPreferenceMongoRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(paperlessPreferenceRepository.drop)
  }

  s"POST /identifier-mapping/$testPreferencesToken" should {
    "store the NINO successfully and return OK" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      val res = IncomeTaxSubscription.storeNino(testPreferencesToken, testNino)

      res should have(
        httpStatus(Status.CREATED)
      )

      val dbRes = await(paperlessPreferenceRepository.find("_id" -> testPreferencesToken)).headOption

      dbRes should contain(PaperlessPreferenceKey(testPreferencesToken, testNino))
    }
  }
}
