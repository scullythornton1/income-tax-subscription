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

package controllers

import javax.inject.Inject

import play.api.mvc.{Action, AnyContent}

class DocumentationController @Inject()(errorHandler: play.api.http.HttpErrorHandler) extends AssetsBuilder(errorHandler) {

  def documentation(version: String, endpointName: String): Action[AnyContent] = {
    super.at(s"/public/api/documentation/$version", s"${endpointName.replaceAll(" ", "-")}.xml")
  }

  def definition(): Action[AnyContent] = {
    super.at(s"/public/api", "definition.json")
  }

  def raml(version: String, file: String): Action[AnyContent] = {
    super.at(s"/public/api/conf/$version", file)
  }
}

