package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import utils.Constants._
import utils.Environment._


object GatewaySessions {

  val userRequestBody = "{\n    \"clientId\": \"" + GATEWAY_CLIENT + "\",\n    \"clientSecret\": \"" + GATEWAY_SECRET + "\",\n    \"grantType\": \"client_credentials\"\n}"

//  val createConsentRequestBody: String = "{\"hipIds\": [\n" + "  \"" + LINKED_PROVIDER + "\" \n" + "    ],\n" + "    \"reloadConsent\": true      \n" + "}"
  print("***********************")
  print(userRequestBody)

  val request: String = "${consentId}"
  val fetchHealthDataRequestBody: String = "{\"requestIds\": [\"" + request + "\"]      \n" + "    \"limit\": 10,\n" + "\"offset\": 0   \n" + "}                                          "

  val gatewaySession: ChainBuilder = exec(
    http("Gateway Sessions")
      .post("/gateway/v0.5/sessions")
      .body(StringBody(userRequestBody))
      .check(status.is(200))
  )

  val gatewaySessionsAtOnce: ScenarioBuilder =
    scenario("Create session on gateway")
      .exec(gatewaySession)
}
