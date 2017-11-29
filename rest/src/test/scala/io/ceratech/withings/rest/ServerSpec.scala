package io.ceratech.withings.rest

import org.scalatest.WordSpec

/**
  * Test if the server will startup without any errors
  *
  * @author dries
  */
class ServerSpec extends WordSpec {

  "The server" should {
    "startup" in {
      val server = Server
      server.main(Array())
      succeed
    }
  }

}
