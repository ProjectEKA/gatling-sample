package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import scenarios.FetchPatientHealthRecords
import utils.Constants._
import utils.Environment._

class FetchPatientHealthRecordsAtOnceUsers extends Simulation {
  /* Place for arbitrary Scala code that is to be executed before the simulation begins. */
  before {
    println("***** My simulation is about to begin! *****")
    println(BASE_URL)
  }

  /* Place for arbitrary Scala code that is to be executed after the simulation has ended. */
  after {
    println("***** My simulation has ended! ******")
  }

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(BASE_URL)
    .header(CONTENT_TYPE, APPLICATION_JSON)

  setUp(
    FetchPatientHealthRecords.fetchUserHealthRecords.inject(atOnceUsers(t_users)).protocols(httpProtocol)
  )
}

