package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios.CreateConsentRequestForAPatient
import utils.Constants._
import utils.Environment.{cmBaseUrl, nhaBaseUrl}

class ConsentRequestCreationConstantUsersFor10seconds extends Simulation {
  /* Place for arbitrary Scala code that is to be executed before the simulation begins. */
  before {
    println("***** My simulation is about to begin! *****")
  }

  /* Place for arbitrary Scala code that is to be executed after the simulation has ended. */
  after {
    println("***** My simulation has ended! ******")
  }

  val httpProtocol = http
    .baseUrl(nhaBaseUrl)
    .header(CONTENT_TYPE, APPLICATION_JSON)

  val cmHttpProtocol = http
    .baseUrl(cmBaseUrl)
    .header(CONTENT_TYPE, APPLICATION_JSON)

  setUp(CreateConsentRequestForAPatient.scenarios.inject(constantUsersPerSec(10) during (10)).protocols(httpProtocol))

}

