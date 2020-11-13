package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import utils.Constants._
import utils.Environment
import utils.Environment._


object DenyConsentRequest {

  val loginRequestBody: String = "{\n\t\"username\": \"" + HIU_USERNAME + "\",\n\t\"password\": \"" + HIU_PASSWORD + "\"\n}"
  val createConsentRequestBody: String = "{\n    \"consent\": {\n        \"patient\": {\n            \"id\": \"" + USERNAME + "\"\n        },\n        \"purpose\": {\n            \"code\": \"CAREMGT\"\n        },\n        \"hiTypes\": [\n            \"OPConsultation\"\n        ],\n        \"permission\": {\n            \"dateRange\": {\n                \"from\": \"1992-04-03T10:05:26.352Z\",\n                \"to\": \"2020-08-08T10:05:26.352Z\"\n            },\n            \"dataEraseAt\": \"2020-10-30T12:30:00.352Z\"\n        }\n    }\n}"
  val userRequestBody: String = "{\"grantType\":\"password\",\"password\":\"" + PASSWORD + "\",\"username\":\"" + USERNAME + "\"}"


  val hiuUserLogin: ChainBuilder = exec(
    http("create hiu session")
      .post(HIU_API + "/sessions")
      .body(StringBody(loginRequestBody))
      .check(status.is(200))
      .check(jsonPath("$.accessToken").findAll.saveAs("accessToken"))
  )

  val fetchPatientInfo: ChainBuilder = exec(
    http("patient info")
      .get(HIU_API + "/v1/patients/" + Environment.USERNAME + "?")
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

  val getConsentRequestId: ChainBuilder = exec(
    http("get consent request id")
      .get(HIU_API + "/v1/hiu/consent-requests")
      .header(HIU_AUTHORIZATION, "${accessToken}")
      .check(status.is(200))
      .check(jmesPath("[0].[consentRequestId] | [0]").saveAs("consentRequestId"))
  ).exec(session => {
    val body = session("consentRequestId").as[String]
    println("CONSENT REQUEST ------> " + body)
    session
  })

  val userLogin: ChainBuilder = exec(
    http("create patient session")
      .post("/cm/sessions")
      .body(StringBody(userRequestBody))
      .check(status.is(200))
      .check(jsonPath("$.token").findAll.saveAs("userAccessToken"))
  )

  val denyHIUConsentRequest: ChainBuilder = exec(
    http("deny consent request")
      .post("/cm/consent-requests/${consentRequestId}/deny")
      .header(AUTHORIZATION, "${userAccessToken}")
      .check(status.is(204))
      .check(bodyString.saveAs("BODY"))
  ).exec(session => {
    val body = session("BODY").as[String]
    println("DENY ------> " + body)
    session
  })

  val denyConsentRequest: ScenarioBuilder =
    scenario("Deny consent request")
      .exec(hiuUserLogin,
        fetchPatientInfo, createConsentRequest,
        getConsentRequestId, userLogin, denyHIUConsentRequest)


}
