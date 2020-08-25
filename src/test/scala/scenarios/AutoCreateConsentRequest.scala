package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import utils.Constants._
import utils.Environment


object AutoCreateConsentRequest {

  val userRequestBody = "{\"grantType\":\"password\",\"password\":\"" + Environment.password + "\",\"username\":\"" + Environment.username + "\"}"

  val createConsentRequestBody: String = "{\"hipIds\": [\n" + "  \""+ LINKED_PROVIDER +"\" \n" + "    ],\n" + "    \"reloadConsent\": true      \n" + "}"


  val userLogin: ChainBuilder = exec(
    http("create session")
      .post("/cm/sessions")
      .body(StringBody(userRequestBody))
      .check(status.is(200))
      .check(jsonPath("$.token").findAll.saveAs("userAccessToken"))
  )

  val autoCreateConsentRequest: ChainBuilder = exec(
    http("auto create consent request")
      .post("/cm/v1/patient/consent-request")
      .header(AUTHORIZATION, "${userAccessToken}")
      .body(StringBody(createConsentRequestBody))
      .check(status.is(202))
      .check(jsonPath("$." + LINKED_PROVIDER).findAll.saveAs("requestId"))
      .check(bodyString.saveAs("BODY"))
        ).exec(session => {
          val body = session("BODY").as[String]
          println(body)
          session
        }
  )

  val fetchStatusRequestBody: String = "{\"requestIds\": [\"${requestId}\"]      \n" + "}"

  val fetchHealthDataRequestBody: String = "{\"requestIds\": [\"${requestId}\"]      \n" + "    \"limit\": 10,\n" + "\"offset\": 0   \n" + "}                                          "

  val fetchConsentStatus: ChainBuilder = exec(
    http("fetch consent-request status")
      .post("/cm/v1/patient/health-information/status")
      .header(AUTHORIZATION, "${userAccessToken}")
      .body(StringBody(fetchStatusRequestBody))
      .check(status.is(200))
//      .check(jsonPath("$.status").is("SUCCEEDED"))
      .check(bodyString.saveAs("BODY"))
        ).exec(session => {
          val body = session("BODY").as[String]
          println(body)
          session
        }
  )

  val fetchHealthData: ChainBuilder = exec(
    http("fetch health-data")
      .post("/cm/v1/patient/health-information/fetch")
      .header(AUTHORIZATION, "${userAccessToken}")
      .body(StringBody(fetchHealthDataRequestBody))
      .check(status.is(200))
      .check(bodyString.saveAs("BODY"))
        ).exec(session => {
          val body = session("BODY").as[String]
          println(body)
          session
        }
  )

  val autoCreateConsentScenario: ScenarioBuilder =
    scenario("Auto Create Consent Request from CM and fetch user health-records")
      .exec(userLogin, autoCreateConsentRequest, fetchConsentStatus, fetchHealthData)
}
