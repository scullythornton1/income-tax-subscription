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

package unit.models.registration

import models.registration._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.JsonUtil._

class RegistrationResponseModelSpec extends UnitSpec {

  "RegistrationResponseModel" should {
    "Reads the safe id correctly from a successful registration response" in {
      val safeId = "XE0001234567890"
      val response: JsValue =
        s"""
           |{
           |  "safeId": "$safeId",
           |  "agentReferenceNumber": "AARN1234567",
           |  "isEditable": true,
           |  "isAnAgent": false,
           |  "isAnIndividual": true,
           |  "individual": {
           |    "firstName": "Stephen",
           |    "lastName": "Wood",
           |    "dateOfBirth": "1990-04-03"
           |  },
           |  "address": {
           |    "addressLine1": "100 SuttonStreet",
           |    "addressLine2": "Wokingham",
           |    "addressLine3": "Surrey",
           |    "addressLine4": "London",
           |    "postalCode": "DH14EJ",
           |    "countryCode": "GB"
           |  },
           |  "contactDetails": {
           |    "primaryPhoneNumber": "01332752856",
           |    "secondaryPhoneNumber": "07782565326",
           |    "faxNumber": "01332754256",
           |    "emailAddress": "stephen@manncorpone.co.uk"
           |  }
           |}
       """.stripMargin
      Json.fromJson[RegistrationSuccessResponseModel](response).get shouldBe RegistrationSuccessResponseModel(safeId)
    }

    "Reads the error messages correctly from a failure registration response" in {
      val reason = "Service unavailable"
      val response: JsValue =
        s"""
           {
           |    "reason":"$reason"
           |}
       """.stripMargin
      Json.fromJson[RegistrationFailureResponseModel](response).get shouldBe RegistrationFailureResponseModel(reason)
    }
  }

}
