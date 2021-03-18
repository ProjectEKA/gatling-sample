package scenarios

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import utils.Environment._


object ConsentRequestOnCM {

  val gatewaySessionBody = "{\n    \"clientId\": \"" + GATEWAY_CLIENT + "\",\n    \"clientSecret\": \"" + GATEWAY_SECRET + "\",\n    \"grantType\": \"client_credentials\"\n}"
  val consentRequestOnCMBody = "{\"requestId\": \"" + UUID.randomUUID().toString + "\",\"timestamp\": \"" + LocalDateTime.now(ZoneOffset.UTC) + "\"," +
    "\"consent\":{\"" +
    "purpose\":{\"text\":\"Care Managerment\",\"code\":\"CAREMGT\"},\"" +
    "patient\":{\"id\":\"mdubey@sbx\"},\"" +
    "hiu\":{\"id\":\"10000002\",\"name\":\"Project EKA HIU\"},\"" +
    "requester\":{\"name\":\"Dr Manju\"},\"" +
    "hiTypes\":[\"Prescription\",\"OPConsultation\",\"DiagnosticReport\",\"DischargeSummary\",\"OPConsultation\"],\"" +
    "permission\":{\"" +
    "accessMode\":\"VIEW\",\"" +
    "dateRange\":{\"from\":\"1992-04-03T10:05:26.352Z\",\"to\":\"2020-08-08T10:05:26.352Z\"},\"" +
    "dataEraseAt\":\"2025-10-30T12:30:00.352Z\",\"" +
    "frequency\":{\"unit\":\"HOUR\",\"value\":12,\"repeats\":2" +
    "}}}}"

  val gatewayLogin: ChainBuilder = exec(
    http("create session")
      .post("/gateway/sessions")
      .body(StringBody(gatewaySessionBody))
      .check(status.is(200))
      .check(jmesPath("[token] | [0]").saveAs("gatewayAccessToken"))
  )

  //Replace below with a session token from GW
  val GW_ACCESS_TOKEN = "";

    val createConsentRequest: ChainBuilder = exec(http("Create consent request on CM")
      .post("/cm/v0.5/consent-requests/init")
      .header("Authorization", "Bearer " + GW_ACCESS_TOKEN)
      .body(StringBody(consentRequestOnCMBody))
      .check(status.is(202))
//      .check( bodyString.saveAs( "RESPONSE_DATA" ) )
    )
//      .exec( session => {
//        println("Some Restful Service Response Body:")
//        println(session("RESPONSE_DATA").as[String])
//        session
//      })


  val createConsentRequests: ScenarioBuilder =
    scenario("Create Consent Request")
      .exec(createConsentRequest)
}
