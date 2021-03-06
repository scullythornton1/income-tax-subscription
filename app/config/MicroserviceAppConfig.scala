/*
 * Copyright 2018 HM Revenue & Customs
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

package config

import javax.inject.{Inject, Singleton}

import config.featureswitch.FeatureSwitching
import play.api.{Application, Configuration}
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val authURL: String
  val ggURL: String
  val ggAdminURL: String
  val ggAuthenticationURL: String
  def desURL: String
  val desEnvironment: String
  val desToken: String
  val paperlessPreferencesExpirySeconds: Int
}

@Singleton
class MicroserviceAppConfig @Inject()(val app: Application) extends AppConfig with ServicesConfig with FeatureSwitching {
  val configuration = app.configuration
  override val mode = app.mode

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  override lazy val authURL = baseUrl("auth")
  override lazy val ggAuthenticationURL = baseUrl("gg-authentication")
  override lazy val ggURL = baseUrl("government-gateway")
  override lazy val ggAdminURL = baseUrl("gg-admin")

  private def desBase =
    if (isEnabled(featureswitch.StubDESFeature)) "microservice.services.stub-des"
    else "microservice.services.des"

  override def desURL = loadConfig(s"$desBase.url")

  override lazy val desEnvironment = loadConfig(s"$desBase.environment")
  override lazy val desToken = loadConfig(s"$desBase.authorization-token")
  override val paperlessPreferencesExpirySeconds: Int = {
    val key = s"paperless-preference.expiry-seconds"
    configuration.getInt(s"paperless-preference.expiry-seconds")
      .getOrElse(throw new Exception(s"Missing configuration key: $key"))
  }

  override protected def runModeConfiguration: Configuration = configuration
}
