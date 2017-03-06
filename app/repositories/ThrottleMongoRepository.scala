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

import utils.Implicits._
import models.throttling.UserCount
import play.api.libs.json.JsValue
import reactivemongo.api.DB
import reactivemongo.bson.{BSONArray, _}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{ReactiveRepository, Repository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ThrottleRepository extends Repository[UserCount, BSONObjectID] {

  def checkAndUpdate(date: String, threshold: Int, internalId: String): Future[Int]

}

class ThrottleMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[UserCount, BSONObjectID]("throttle", mongo, UserCount.formats, ReactiveMongoFormats.objectIdFormats)
    with ThrottleRepository {

  def checkAndUpdate(date: String, threshold: Int, internalId: String): Future[Int] = {
    val selector = BSONDocument("_id" -> date)
    collection.find(selector = selector).
      cursor[UserCount]().collect[List]().flatMap {
      users =>
        users.nonEmpty && users.head.users.contains(internalId) match {
          case true => users.size
          case false =>
            val modifier =
              BSONDocument("$push" -> BSONDocument("users" -> internalId), "$set" -> BSONDocument("threshold" -> threshold))
            collection.findAndUpdate(selector, modifier, fetchNewObject = true, upsert = true) map {
              _.result[JsValue] match {
                case None => -1
                case Some(res) => (res \ "users").as[Set[String]].size
              }
            }
        }
    }

  }

}
