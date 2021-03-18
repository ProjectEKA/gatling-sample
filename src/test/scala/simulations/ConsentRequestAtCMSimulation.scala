package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import scenarios.{AutoCreateConsentRequest, ConsentRequestOnCM}
import utils.Constants._
import utils.Environment._

class ConsentRequestAtCMSimulation extends Simulation {
  /* Place for arbitrary Scala code that is to be executed before the simulation begins. */
  before {
    println("***** My simulation is about to begin! *****")
  }

  /* Place for arbitrary Scala code that is to be executed after the simulation has ended. */
  after {
    println("***** My simulation has ended! ******")
  }

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(BASE_URL)
    .header(CONTENT_TYPE, APPLICATION_JSON)

  setUp(
    ConsentRequestOnCM
      .createConsentRequests
      .inject(atOnceUsers(t_users))
      .protocols(httpProtocol)

  )
}

