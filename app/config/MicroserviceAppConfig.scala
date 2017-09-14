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

package config

import javax.inject.{Inject,Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val authURL: String
  val ggURL: String
  val ggAdminURL: String
  val ggAuthenticationURL: String
  val desURL: String
  val desEnvironment: String
  val desToken: String
  val paperlessPreferencesExpirySeconds: Int
}

@Singleton
class MicroserviceAppConfig @Inject()(val configuration: Configuration) extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  override lazy val authURL = baseUrl("auth")
  override lazy val ggAuthenticationURL = baseUrl("gg-authentication")
  override lazy val ggURL = baseUrl("government-gateway")
  override lazy val ggAdminURL = baseUrl("gg-admin")

  lazy val desBase = configuration.getString("feature-switching.useNewDesRoute").fold(false)(x=>x.toBoolean) match {
    case true =>"microservice.services.new-des"
    case _ =>"microservice.services.des"
  }
  override lazy val desURL = loadConfig(s"$desBase.url")
  override lazy val desEnvironment = loadConfig(s"$desBase.environment")
  override lazy val desToken = loadConfig(s"$desBase.authorization-token")
  override val paperlessPreferencesExpirySeconds: Int = {
    val key = s"paperless-preference.expiry-seconds"
      configuration.getInt(s"paperless-preference.expiry-seconds")
        .getOrElse(throw new Exception(s"Missing configuration key: $key"))
  }
}
