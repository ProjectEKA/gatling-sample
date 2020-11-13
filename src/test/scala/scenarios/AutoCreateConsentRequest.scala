package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import utils.Constants._
import utils.Environment
import utils.Environment._


object AutoCreateConsentRequest {

  val userRequestBody = "{\"grantType\":\"password\",\"password\":\"" + PASSWORD + "\",\"username\":\"" + USERNAME + "\"}"

  val createConsentRequestBody: String = "{\"hipIds\": [\n" + "  \"" + LINKED_PROVIDER + "\" \n" + "    ],\n" + "    \"reloadConsent\": true      \n" + "}"


  val userLogin: ChainBuilder = exec(
    http("create session")
      .post("/cm/sessions")
      .body(StringBody(userRequestBody))
      .check(status.is(200))
      .check(jmesPath("[token] | [0]").saveAs("userAccessToken"))
  )


  val autoCreateConsentRequest: ChainBuilder = exec(
    http("auto create consent request")
      .post("/cm/v1/patient/consent-request")
      .header(AUTHORIZATION, "${userAccessToken}")
      .body(StringBody(createConsentRequestBody))
      .check(status.is(202))
      .check(jmesPath("[" + LINKED_PROVIDER + "] | [0]").saveAs("consentId"))
  )

  val request: String = "${consentId}"
  val fetchHealthDataRequestBody: String = "{\"requestIds\": [\"" + request + "\"]      \n" + "    \"limit\": 10,\n" + "\"offset\": 0   \n" + "}                                          "

  val fetchConsentStatus: ChainBuilder = exec(
    http("fetch consent-request status")
      .post("/cm/v1/patient/health-information/status")
      .header(AUTHORIZATION, "${userAccessToken}")
      //      .body(StringBody(fetchStatusRequestBody))
      .body(StringBody("{'requestIds': [\""+request+"\"]     }"))
      .check(status.is(200))
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
      .exec(userLogin)
      .exec(autoCreateConsentRequest)
      .pause(30)
      .exec(fetchConsentStatus)
  //  , fetchHealthData)
}
