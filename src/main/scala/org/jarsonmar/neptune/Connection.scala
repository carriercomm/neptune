package org.jarsonmar.neptune

import akka.actor.IO
import akka.util.ByteString

import scala.collection.mutable.{Map,HashMap}

class Connection(socket: IO.SocketHandle) {

  val state: Map[String, String] = new HashMap[String, String]()

  private def setState(key: String, value: String) = state.put(key, value)
  private def getState(key: String): Option[String] = state.get(key)
  private def hasState(key: String): Boolean = state.contains(key)

  def setName(name: String) = setState("name", name)
  def getName: Option[String] = getState("name")
}
