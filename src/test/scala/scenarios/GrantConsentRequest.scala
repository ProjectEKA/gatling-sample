package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import scenarios.DenyConsentRequest.{createConsentRequestBody, loginRequestBody, userRequestBody}
import utils.Constants._
import utils.{Constants, Environment}


object GrantConsentRequest {

  val requestId = java.util.UUID.randomUUID.toString
  val loginRequestBody = "{\n\t\"username\": \"admin\",\n\t\"password\": \"tzDvMGYvEM3SFHB6\"}"
  val createConsentRequestBody: String = "{\n    \"consent\": {\n        \"patient\": {\n            \"id\": \"" + Environment.username + "\"\n        },\n        \"purpose\": {\n            \"code\": \"CAREMGT\"\n        },\n        \"hiTypes\": [\n            \"OPConsultation\"\n        ],\n        \"permission\": {\n            \"dateRange\": {\n                \"from\": \"1992-04-03T10:05:26.352Z\",\n                \"to\": \"2020-08-08T10:05:26.352Z\"\n            },\n            \"dataEraseAt\": \"2020-10-30T12:30:00.352Z\"\n        }\n    }\n}"
  val userRequestBody: String = "{\"grantType\":\"password\",\"password\":\"" + Environment.password + "\",\"username\":\"" + Environment.username + "\"}"
  val grantConsentRequestBody: String = " {\"consents\": [\n" + "       {\n" + "\"careContexts\": [{\"careContextReference\": \"NCP1007\",    \n" + "                    \"patientReference\": \"RVH1002\"      \n" + "                },                                         \n" + "                {                                          \n" + "                    \"careContextReference\": \"RV-MHD-01.17.0024\",    \n" + "                    \"patientReference\": \"RVH1002\"      \n" + "                }                                          \n" + "           ],                                              \n" + "           \"hiTypes\": [                                  \n" + "               \"Prescription\"                            \n" + "           ],                                              \n" + "           \"hip\": {                                      \n" + "               \"id\": \"10000005\"                        \n" + "           },                                              \n" + "           \"permission\": {                               \n" + "               \"accessMode\": \"VIEW\",                   \n" + "               \"dataEraseAt\": \"2020-12-27T10:45:54.688\",    \n" + "               \"dateRange\": {                            \n" + "                   \"from\": \"1992-06-25T18:30:00\",      \n" + "                   \"to\": \"2020-06-26T10:45:54.688\"     \n" + "               },                                          \n" + "               \"frequency\": {                            \n" + "                   \"value\": 1,                           \n" + "                   \"unit\": \"HOUR\",                     \n" + "                   \"repeats\": 0                          \n" + "               }                                           \n" + "           }                                               \n" + "       }                                                   \n" + "   ]                                                       \n" + "}                                                          "
  val approvePINRequestBody: String = "{\"pin\": \"1234\",\n" + "   \"requestId\": \"" + requestId + "\"" + ",\n" + "   \"scope\": \"consentrequest.approve\" \n" + "}"


  val hiuUserLogin: ChainBuilder = exec(
    http("create hiu session")
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
  )

  val getConsentRequestId: ChainBuilder = exec(
    http("get consent request id")
      .get("/api-hiu/v1/hiu/consent-requests")
      .header("Authorization", "${accessToken}")
      .check(status.is(200))
      //      .check(jsonPath("$..[0].status").is("REQUESTED"))
      .check(jmesPath("[0].[consentRequestId] | [0]").saveAs("consentRequestId"))
  ).exec(session => {
    val body = session("consentRequestId").as[String]
    println("CONSENT REQUEST ID ------> "+body)
    session
  })

  val userLogin: ChainBuilder = exec(
    http("create patient session")
      .post("/cm/sessions")
      .body(StringBody(userRequestBody))
      .check(status.is(200))
      .check(jsonPath("$.token").findAll.saveAs("userAccessToken"))
  )

  val userPINAuthorization: ChainBuilder = exec(
    http("validate approve PIN")
      .post("/cm/patients/verify-pin")
      .header(AUTHORIZATION, "${userAccessToken}")
      .body(StringBody(approvePINRequestBody))
      .check(status.is(200))
      .check(jsonPath("$.temporaryToken").findAll.saveAs("pinAccessToken"))
      .check(bodyString.saveAs("BODY"))
  ).exec(session => {
    val body = session("BODY").as[String]
    println("BODY ------> "+body)
    session
  })
//  )

  val grantConsentRequest: ChainBuilder = exec(
    http("grant consent request")
      .post("/cm/consent-requests/${consentRequestId}/approve")
      .body(StringBody(grantConsentRequestBody))
      .header("Authorization", "${pinAccessToken}")
      .check(status.is(204))
        .check(bodyString.saveAs("BODY"))
      ).exec(session => {
        val body = session("BODY").as[String]
        println("BODY ------> "+body)
        session
      })
//  )

  val grantConsentRequestScenario: ScenarioBuilder =
    scenario("Fetch patient information by HIU")
      .exec(hiuUserLogin, fetchPatientInfo, createConsentRequest, getConsentRequestId, userLogin, userPINAuthorization, grantConsentRequest)
}
