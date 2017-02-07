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

package utils

import scala.concurrent.Future


trait Implicits {

  implicit def OptionUtl[T, S <: T](data: S): Option[T] = Some(data)

  implicit def FOptionUtl[T, S <: T](data: S): Future[Option[T]] = Future.successful(data)

  implicit def EitherUtilLeft[T, S <: T, L](data: S): Either[T, L] = Left(data)

  implicit def EitherUtilRight[T, S <: T, R](data: S): Either[R, T] = Right(data)

  implicit def FEitherUtilLeft[T, S <: T, L](data: S): Future[Either[T, L]] = Future.successful(data)

  implicit def FEitherUtilRight[T, S <: T, R](data: S): Future[Either[_, R]] = Future.successful(data)

}

object Implicits extends Implicits
