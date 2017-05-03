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

package repositories

import models.throttling.UserCount
import reactivemongo.api.DB
import reactivemongo.bson.BSONDocument
import repositories.ThrottleMongoRepository
import uk.gov.hmrc.time.DateTimeUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestThrottleMongoRepository(implicit mongo: () => DB) extends ThrottleMongoRepository {

  def dateTime: String = DateTimeUtils.now.toString("yyyy-MM-dd")

  def collectionExists: Future[Boolean] = collection.count().map(c => c != 0)

  val selector = BSONDocument("_id" -> dateTime)

  def userCount: Future[Int] =
    collection.find(selector = selector).cursor[UserCount]().collect[List]().map {
      case Nil => 0
      case head :: _ => head.users.size
    }

}
