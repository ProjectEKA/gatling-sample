package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import scenarios.DenyConsentRequest
import utils.Constants._
import utils.Environment
import utils.Environment._

class DenyConsentRequestAtOnceUsers extends Simulation {
  /* Place for arbitrary Scala code that is to be executed before the simulation begins. */
  before {
    println("***** My simulation is about to begin! *****")
  }

  /* Place for arbitrary Scala code that is to be executed after the simulation has ended. */
  after {
    println("***** My simulation has ended! ******")
  }

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .header(CONTENT_TYPE, APPLICATION_JSON)

  setUp(
    DenyConsentRequest.denyConsentRequest
      .inject(atOnceUsers(t_users))
//      .inject(constantConcurrentUsers(10) during (10)).throttle(reachRps(10) in (10))
//        .inject(rampUsers(10) during (10),nothingFor(5))
//      .throttle(jumpToRps(20), holdFor(2))
      .protocols(httpProtocol))
}

