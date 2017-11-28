package io.ceratech.withings.amqp

import com.typesafe.config.ConfigFactory
import pureconfig.loadConfig

/**
  * RPC server
  *
  * @author dries
  */
object RpcServer extends App {

  private lazy val rpcConfig: RpcConfig = loadConfig[RpcConfig](ConfigFactory.load(), "amqp") match {
    case Left(errors) ⇒ throw new IllegalStateException(s"Configuration error(s): ${errors.toList.map(_.description).mkString(", ")}")
    case Right(v) ⇒ v
  }



}
