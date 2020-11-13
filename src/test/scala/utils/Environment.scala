package utils

object Environment {
  val BASE_URL = System.getProperty("baseUrl")
  val USERNAME = System.getProperty("username")
  val PASSWORD = System.getProperty("password")
  val AUTHORIZATION = System.getProperty("authorization")
  val HIU_AUTHORIZATION = System.getProperty("hiu_authorization")
  val HIU_API = System.getProperty("hiu_api")
  val HIU_USERNAME = System.getProperty("hiu_username")
  val HIU_PASSWORD = System.getProperty("hiu_password")

  val t_users = Integer.getInteger("users", 1).toInt
  val t_rampUp = Integer.getInteger("rampup", 1).toInt
  val t_holdFor = Integer.getInteger("duration", 10).toInt
  val t_throughput = Integer.getInteger("throughput", 100).toInt
}
