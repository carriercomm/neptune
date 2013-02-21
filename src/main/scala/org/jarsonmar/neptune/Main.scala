package org.jarsonmar.neptune

import akka.actor._

object Controller extends App {
  val system = ActorSystem("NeptuneController")
  val server = system.actorOf(Props[controller.Dispatcher])
}

object Universe extends App {
  val system = ActorSystem("NeptuneUniverse")
  val server = system.actorOf(Props[controller.Dispatcher]) // XXX universe.Dispatcher
}
