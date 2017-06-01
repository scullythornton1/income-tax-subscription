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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{MappingBuilder, ResponseDefinitionBuilder}
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import helpers.WireMockDSL.HTTPVerbMapping.{Get, HTTPVerbStub, Post}
import play.api.http.Status
import play.api.libs.json.{JsValue, Writes}

import scala.reflect.ClassTag


object WireMockDSL {
  val stub = WireMockStub

  def multiline(stub: StubMapping): StubMapping = stub

  def when(verbStub: HTTPVerbStub): WireMockStub = stub.when(verbStub)

  class WireMockStub(mapping: => MappingBuilder) {
    def thenReturn[T](value: T)(implicit writes: Writes[T]): StubMapping = thenReturn(writes.writes(value))

    def thenReturn(jsValue: JsValue): StubMapping =
      createStubFor(
        aResponse()
          .withStatus(Status.OK).withBody(jsValue.toString)
      )

    private def createStubFor(response: ResponseDefinitionBuilder) = {
      stubFor(mapping.willReturn(response))
    }

    def thenReturn(status: Int): StubMapping =
      createStubFor(
        aResponse()
          .withStatus(status)
      )
  }

  object WireMockStub {
    def when(verbStub: HTTPVerbStub): WireMockStub = new WireMockStub(verbStub.mapping)

    def verify(verbStub: HTTPVerbStub): Unit = verbStub match {
      case Get(uri) => WiremockHelper.verifyGet(uri)
      case req @ Post(uri, _) => WiremockHelper.verifyPost(uri, req.jsonString)
      case _ => ()
    }
  }

  object HTTPVerbMapping {

    sealed trait HTTPVerbStub {
      val uri: String

      val mapping: MappingBuilder
    }

    case class Get(uri: String) extends HTTPVerbStub {
      override val mapping = get(urlMatching(uri))
    }

    case class Put(uri: String) extends HTTPVerbStub {
      override val mapping = put(urlMatching(uri))
    }

    case class Post[T] private(uri: String, optBody: Option[T])(implicit writes: Writes[T]) extends HTTPVerbStub {
      override val mapping = {
        val uriMapping = post(urlMatching(uri))
        optBody match {
          case Some(body) => uriMapping.withRequestBody(new EqualToPattern(writes.writes(body).toString()))
          case None => uriMapping
        }
      }

      val jsonString = optBody map (writes.writes(_).toString)
    }

    object Post {
      def apply(uri: String): Post[String] = new Post[String](uri, None)

      def apply[T](uri: String, body: T)(implicit writes: Writes[T]): Post[T] = new Post[T](uri, Some(body))
    }

  }

}

