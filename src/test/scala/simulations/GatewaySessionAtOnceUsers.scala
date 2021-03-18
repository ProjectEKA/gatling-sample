package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import scenarios.GatewaySessions
import utils.Constants._
import utils.Environment._

import scala.concurrent.duration.DurationInt

class GatewaySessionAtOnceUsers extends Simulation {
  /* Place for arbitrary Scala code that is to be executed before the simulation begins. */
  before {
    print(BASE_URL)
    print(GATEWAY_CLIENT)
    print(GATEWAY_SECRET)
    println("***** Simulation for Gateway Sessions starting! *****")
  }

  /* Place for arbitrary Scala code that is to be executed after the simulation has ended. */
  after {
    println("***** Simulation for Gateway Sessions ended! ******")
  }

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(BASE_URL)
    .header(CONTENT_TYPE, APPLICATION_JSON)

  setUp(
    GatewaySessions
      .gatewaySessionsAtOnce
      .inject(
          atOnceUsers(t_users)
//        ,constantUsersPerSec(200).during(120.seconds)
      )
      .protocols(httpProtocol)

  )
}

