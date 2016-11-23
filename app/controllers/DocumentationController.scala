package controllers

import uk.gov.hmrc.play.microservice.controller.BaseController

trait Documentation extends AssetsBuilder with BaseController {

  def documentation(version: String, endpointName: String) = {
    super.at(s"/public/api/documentation/$version", s"${endpointName.replaceAll(" ", "-")}.xml")
  }

  def definition() = {
    super.at(s"/public/api", "definition.json")
  }

  def raml(version: String, file: String) = {
    super.at(s"/public/api/conf/$version", file)
  }
}

object Documentation extends Documentation
