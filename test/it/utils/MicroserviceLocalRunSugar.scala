package it.utils

import play.api.Play
import play.api.test.FakeApplication

trait MicroserviceLocalRunSugar {

  val additionalConfiguration: Map[String, Any]

  lazy val fakeApplication = FakeApplication(additionalConfiguration = additionalConfiguration)

  def run(block: () => Unit) = {
    Play.start(fakeApplication)
    block()
    Play.stop()
  }
}
