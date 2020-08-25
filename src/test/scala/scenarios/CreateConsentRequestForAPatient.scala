package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import utils.Environment
import utils.Environment._


object CreateConsentRequestForAPatient {

  val loginRequestBody: String = "{\n\t\"username\": \"admin\",\n\t\"password\": \"tzDvMGYvEM3SFHB6\"\n}"
  val createConsentRequestBody: String = "{\n    \"consent\": {\n        \"patient\": {\n            \"id\": \"" + username + "\"\n        },\n        \"purpose\": {\n            \"code\": \"CAREMGT\"\n        },\n        \"hiTypes\": [\n            \"OPConsultation\"\n        ],\n        \"permission\": {\n            \"dateRange\": {\n                \"from\": \"1992-04-03T10:05:26.352Z\",\n                \"to\": \"2020-08-08T10:05:26.352Z\"\n            },\n            \"dataEraseAt\": \"2020-10-30T12:30:00.352Z\"\n        }\n    }\n}"

  val hiuUserLogin: ChainBuilder = exec(
    http("create session")
      .post("/api-hiu/sessions")
      .body(StringBody(loginRequestBody))
      .check(status.is(200))
      .check(jsonPath("$.accessToken").findAll.saveAs("accessToken"))
  )

  val fetchPatientInfo: ChainBuilder = exec(
    http("patient info")
      .get("/api-hiu/v1/patients/" + Environment.username + "?")
      .header("Authorization", "${accessToken}")
      .check(status.is(200))
  )

  val createConsentRequest: ChainBuilder = exec(
    http("create consent request")
      .post("/api-hiu/v1/hiu/consent-requests")
      .header("Authorization", "${accessToken}")
      .body(StringBody(createConsentRequestBody))
      .check(status.is(202))
      .check(bodyString.saveAs("BODY"))
  ).exec(session => {
    val body = session("BODY").as[String]
    println(body)
    session
  })

  val getConsentRequestId: ChainBuilder = exec(
    http("get consent request id")
      .get("/api-hiu/v1/hiu/consent-requests")
      .header("Authorization", "${accessToken}")
      .check(status.is(200))
      .check(jmesPath("[0].[consentRequestId] | [0]").saveAs("request"))
  )

  val createConsentRequestScenario: ScenarioBuilder =
    scenario("Fetch patient information by HIU")
      .exec(hiuUserLogin, fetchPatientInfo, createConsentRequest
        , getConsentRequestId)
}
