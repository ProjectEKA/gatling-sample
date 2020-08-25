package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import utils.Constants._
import utils.Environment._


object DiscoverHIPForPatient {

  val userRequestBody = "{\"grantType\":\"password\",\"password\":\"" + password + "\",\"username\":\"" + username + "\"}"


  val userLogin: ChainBuilder = exec(
    http("create session")
      .post("/cm/sessions")
      .body(StringBody(userRequestBody))
      .check(status.is(200))
      .check(jmesPath("[token] | [0]").saveAs("userAccessToken"))
  )

  val providerDetails: ChainBuilder = exec(
    http("discover HIP for the user")
      .get("/cm/providers/" + LINKED_PROVIDER + "?")
      .header(AUTHORIZATION, "${userAccessToken}")
      .check(status.is(200))
  )

  val discoverHIP: ChainBuilder = exec(
    http("discover care context for the HIP")
      .post("/cm/v1/care-contexts/discover")
      .header(AUTHORIZATION, "${userAccessToken}")
      .body(StringBody("{\n    \"hip\": {\n        \"id\": \"" + LINKED_PROVIDER + "\"\n    },\n    \"requestId\": \"" + java.util.UUID.randomUUID.toString + "\"\n}"))
      .check(status.is(200))
  )


  val discoverHIPScenario: ScenarioBuilder =
    scenario("Fetch patient information by HIU")
      .exec(userLogin)
      .pause(10)
      .exec(providerDetails)
      .pause(10)
      .exec(discoverHIP)
}
