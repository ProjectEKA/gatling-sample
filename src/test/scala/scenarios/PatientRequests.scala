package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder
import utils.Constants._
import utils.Environment._


object PatientRequests {

  //TODO: Replace these values
  val USERNAME = ""
  val PASSWORD = ""

  val userRequestBody: String = "{\"grantType\":\"password\",\"password\":\"" + PASSWORD + "\",\"username\":\"" + USERNAME + "\"}"

  val userLogin: ChainBuilder = exec(
    http("create session")
      .post("/cm/sessions")
      .body(StringBody(userRequestBody))
      .check(status.is(200))
      .check(jmesPath("[token] | [0]").saveAs("userAccessToken"))
  )

  val userDetail: ChainBuilder = exec(
    http("user details")
      .get("/cm/patients/me")
      .header(AUTHORIZATION, "${userAccessToken}")
      .check(status.is(200))
//      .check(bodyString.saveAs("BODY"))
  )

  val patientRequests: ChainBuilder = exec(
    http("Patient combined requests")
      .get("/cm/patients/requests")
      .header(AUTHORIZATION, "${userAccessToken}")
      .check(status.is(200))
  )


  val fetchCombinedRequests: ScenarioBuilder =
    scenario("Login and fetch Profile")
      .exec(
        userLogin, userDetail, patientRequests)
}
