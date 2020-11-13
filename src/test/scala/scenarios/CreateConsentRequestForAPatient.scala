package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import utils.Environment._


object CreateConsentRequestForAPatient {

  val loginRequestBody: String = "{\n\t\"username\": \"" + HIU_USERNAME + "\",\n\t\"password\": \"" + HIU_PASSWORD + "\"\n}"
  val createConsentRequestBody: String = "{\n    \"consent\": {\n        \"patient\": {\n            \"id\": \"" + USERNAME + "\"\n        },\n        \"purpose\": {\n            \"code\": \"CAREMGT\"\n        },\n        \"hiTypes\": [\n            \"OPConsultation\"\n        ],\n        \"permission\": {\n            \"dateRange\": {\n                \"from\": \"1992-04-03T10:05:26.352Z\",\n                \"to\": \"2020-08-08T10:05:26.352Z\"\n            },\n            \"dataEraseAt\": \"2020-10-30T12:30:00.352Z\"\n        }\n    }\n}"
  val userRequestBody: String = "{\"grantType\":\"password\",\"password\":\"" + PASSWORD + "\",\"username\":\"" + USERNAME + "\"}"
  var initialConsentRequestCount: Int = 0
  var newConsentRequestCount: Int = 0

  val cmUserLogin: ChainBuilder = exec(
    http("patient session")
      .post("/cm/sessions")
      .body(StringBody(userRequestBody))
      .check(status.is(200))
      .check(jmesPath("[token] | [0]").saveAs("userAccessToken"))
  )
  val cmGetUserConsentRequests: ChainBuilder = exec(
    http("get consent requests for the patient")
      .get("/cm/consent-requests?status=REQUESTED")
      .header(AUTHORIZATION, "${userAccessToken}")
      .check(status.is(200))
      .check(jmesPath("size").saveAs("consentRequestSize"))
  ).exec(session => {
    initialConsentRequestCount = session("consentRequestSize").as[Int]
    session
  }
  )
  val hiuUserLogin: ChainBuilder = exec(
    http("create session")
      .post(HIU_API + "/sessions")
      .body(StringBody(loginRequestBody))
      .check(status.is(200))
      .check(jsonPath("$.accessToken").findAll.saveAs("accessToken"))
  )
  val fetchPatientInfo: ChainBuilder = exec(
    http("patient info")
      .get(HIU_API + "/v1/patients/" + USERNAME)
      .header(HIU_AUTHORIZATION, "${accessToken}")
      .check(status.is(200))
  )
  val createConsentRequest: ChainBuilder = exec(
    http("create consent request")
      .post(HIU_API + "/v1/hiu/consent-requests")
      .header(HIU_AUTHORIZATION, "${accessToken}")
      .body(StringBody(createConsentRequestBody))
      .check(status.is(202))
  )
  val getUserConsentRequests: ChainBuilder = exec(
    http("get consent requests for the patient")
      .get("/cm/consent-requests?status=REQUESTED")
      .header(AUTHORIZATION, "${userAccessToken}")
      .check(status.is(200))
      .check(jmesPath("size").saveAs("newConsentRequestSize"))
  ).exec(session => {
    newConsentRequestCount = session("newConsentRequestSize").as[Int]
    println("Initial count of requests - " + initialConsentRequestCount)
    println("Total new requests created - " + newConsentRequestCount)
    println("New count of requests - " + (newConsentRequestCount - initialConsentRequestCount))
    session
  })
  val createConsentRequestScenario: ScenarioBuilder =
    scenario("Fetch patient information by HIU")
      .exec(cmUserLogin, cmGetUserConsentRequests, hiuUserLogin, fetchPatientInfo)
      .exec(getUserConsentRequests)
}
