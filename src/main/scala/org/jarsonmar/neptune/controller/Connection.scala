package org.jarsonmar.neptune.controller

import akka.actor.IO
import akka.util.ByteString

import scala.collection.mutable.{Map,HashMap}

class Connection {
  var state: Connection.State = Connection.ExpectName()
  val data: Map[String, String] = new HashMap[String, String]()

  private def setData(key: String, value: String) = data.put(key, value)
  private def getData(key: String): Option[String] = data.get(key)
  private def hasData(key: String): Boolean = data.contains(key)

  def setName(name: String) = setData("name", name)
  def getName: String = getData("name").getOrElse("(unknown)")

  def getState = state
  def expectName = state = Connection.ExpectName()
  def expectGameCommand = state = Connection.ExpectGameCommand()
}

object Connection {
  abstract class State
  sealed case class ExpectName() extends State
  sealed case class ExpectGameCommand() extends State
}
