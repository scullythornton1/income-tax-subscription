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

package unit.connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.{HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future


trait MockHttp extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val mockHttpPost = mock[HttpPost]
  val mockHttpGet = mock[HttpGet]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpPost)
    reset(mockHttpGet)
  }

  def setupMockHttpPost[I](url: Option[String] = None, body: Option[I] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val bodyMatcher = body.fold(ArgumentMatchers.any[I]())(x => ArgumentMatchers.eq(x))
    when(mockHttpPost.POST[I, HttpResponse](urlMatcher, bodyMatcher, ArgumentMatchers.any()
    )(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(status, Some(response))))
  }

  def verifyHttpPost[I](url: Option[String] = None, body: Option[I] = None)(count: Int): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val bodyMatcher = body.fold(ArgumentMatchers.any[I]())(x => ArgumentMatchers.eq(x))
    verify(mockHttpPost, times(count)).POST[I, HttpResponse](urlMatcher, bodyMatcher, ArgumentMatchers.any()
    )(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
  }

  def setupMockHttpPostEmpty(url: Option[String] = None)(status: Int, response: Option[JsValue]): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    when(mockHttpPost.POSTEmpty[HttpResponse](urlMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(status, response)))
  }

  def verifyMockHttpPostEmpty(url: Option[String] = None)(count: Int): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    verify(mockHttpPost, times(count)).POSTEmpty[HttpResponse](urlMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any())
  }

  def setupMockHttpGet(url: Option[String] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))

    when(mockHttpGet.GET[HttpResponse](urlMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(status, Some(response))))
  }

  def setupMockHttpGetWithParams(url: Option[String], params: Option[Seq[(String, String)]])(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val paramsMatcher = params.fold(ArgumentMatchers.any[Seq[(String, String)]]())(x => ArgumentMatchers.eq(x))
    when(mockHttpGet.GET[HttpResponse](urlMatcher, paramsMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(status, Some(response))))
  }
}
