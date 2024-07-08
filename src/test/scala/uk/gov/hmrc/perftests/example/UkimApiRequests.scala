/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.perftests.example

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.http.Predef._
import io.gatling.http.check.header.HttpHeaderCheckType
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration

import scala.util.Random

object UkimApiRequests extends ServicesConfiguration {

  val baseUrl: String           = baseUrlFor("ukim")
  lazy val authUrlLocal: String = s"$authBaseUrl/government-gateway/session/login"
  lazy val authBaseUrl: String  = baseUrlFor("auth-login-stub")

  val bearerToken: String = readProperty("bearerToken", "${accessToken}")

  val postAuthApiSessionLogin: HttpRequestBuilder =
    http("Post to Auth API Session Login")
      .post(authUrlLocal)
      .body(StringBody(authPayload()))
      .header("Content-Type", "application/json")
      .check(saveAuthBearerToken)

  def saveAuthBearerToken: CheckBuilder[HttpHeaderCheckType, Response, String] =
    header(HttpHeaderNames.Authorization).saveAs("authBearerToken")

  def credID: String =
    Array.fill(16)(Random.nextInt(10)).mkString

  def authPayload(): String =
    s"""
       |{
       |  "credId": "$credID",
       |  "affinityGroup": "Individual",
       |  "confidenceLevel": 50,
       |  "credentialStrength": "strong",
       |  "enrolments": []
       |}
       |""".stripMargin

  def linkPayload(
  ): String =
    s"""
       | {
       | "date":"2025-01-21",
       | "eoris":"["GB120000000999","GB120001000919","GB120001000009"]"
       | }
    """.stripMargin

  val postLink: HttpRequestBuilder =
    http("UKIM Link Request")
      .post(s"$baseUrl/link")
      .header("Authorization", s"Bearer $bearerToken")
      .header("Content-Type", "application/json")
      .body(
        StringBody(
          linkPayload(
          )
        )
      )
      .asJson
      .check(status.is(200))
}
