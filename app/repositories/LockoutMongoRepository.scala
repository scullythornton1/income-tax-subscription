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

import java.time._

import models.lockout.CheckLockout
import models.matching.LockoutResponse
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.indexes.IndexType.Ascending
import utils.Implicits._
import reactivemongo.bson._
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
//import scala.concurrent.duration.Duration

class LockoutMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[CheckLockout, BSONObjectID]("lockout", mongo, CheckLockout.formats, ReactiveMongoFormats.objectIdFormats) {

  def getLockoutStatus(arn: String): Future[Option[LockoutResponse]] = {
    val selector = BSONDocument("arn" -> arn)
    lazy val getLockout = collection.find(selector).one[CheckLockout]

    getLockout.map {
      case result@Some(_) => result.map(clock => LockoutResponse(clock.arn))
      case _ => None
    }
  }

  def lockUser(arn: String): Future[Boolean] = {
    val ttl: Duration = Duration.ofMinutes(1)
    val expiryTimestamp = LocalDateTime.ofInstant(Instant.now.plusSeconds(ttl.getSeconds), ZoneId.systemDefault())

    val selector = CheckLockout(arn, expiryTimestamp)
    collection.insert(selector).map {
      case result if result.ok => true
      case result =>
        println(result.toString)
        false
    }
  }

  def dropDb: Future[Unit] = collection.drop()

  implicit val lockIndex = Index(
    Seq((CheckLockout.expiry, IndexType(Ascending.value))),
    name = Some("lockExpires"),
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    version = None,
    options = BSONDocument("expireAfterSeconds" -> 0)
  )

//  collection.indexesManager.ensure(lockIndex)

}
