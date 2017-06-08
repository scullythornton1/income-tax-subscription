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

package helpers

import helpers.servicemocks.WireMockMethods
import models.frontend.FERequest
import models.throttling.UserCount
import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.libs.ws.WSResponse
import play.api.{Application, Environment, Mode}
import reactivemongo.api.commands.WriteResult
import repositories.{Repositories, ThrottleMongoRepository}
import uk.gov.hmrc.play.test.UnitSpec
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.ExecutionContext

trait ComponentSpecBase extends UnitSpec
  with GivenWhenThen with TestSuite
  with GuiceOneServerPerSuite with ScalaFutures with IntegrationPatience with Matchers
  with WiremockHelper with BeforeAndAfterEach with BeforeAndAfterAll with Eventually
  with CustomMatchers with WireMockMethods {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build
  val mockHost = WiremockHelper.wiremockHost
  val mockPort = WiremockHelper.wiremockPort.toString
  val mockUrl = s"http://$mockHost:$mockPort"

  def config: Map[String, String] = Map(
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.des.url" -> mockUrl,
    "microservice.services.gg-admin.host" -> mockHost,
    "microservice.services.gg-admin.port" -> mockPort,
    "microservice.services.government-gateway.host" -> mockHost,
    "microservice.services.government-gateway.port" -> mockPort,
    "microservice.services.gg-authentication.host" -> mockHost,
    "microservice.services.gg-authentication.port" -> mockPort,
    "microservice.services.throttle-threshold" -> "2"
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(IncomeTaxSubscription.dropThrottleRepo())
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  lazy val throttleMongoRepository = app.injector.instanceOf[Repositories].throttleRepository

  object IncomeTaxSubscription {
    def getSubscriptionStatus(nino: String): WSResponse = get(s"/subscription/$nino")

    def get(uri: String): WSResponse = await(buildClient(uri).get())

    def createSubscription(body: FERequest): WSResponse = post(s"/subscription/${body.nino}", body)

    def checkUserAccess(nino: String): WSResponse = get(s"/throttle/$nino")

    def post[T](uri: String, body: T)(implicit writes: Writes[T]): WSResponse = {
      await(
        buildClient(uri)
          .withHeaders("Content-Type" -> "application/json")
          .post(writes.writes(body).toString())
      )
    }

    def insertUserCount(userCount: UserCount): WriteResult = await(throttleMongoRepository.insert(userCount))
    def dropThrottleRepo(): Unit = await(throttleMongoRepository.drop)
  }

}
