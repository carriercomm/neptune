package org.jarsonmar.neptune

import akka.actor._

object Controller extends App {
  val system = ActorSystem("NeptuneController")
  val server = system.actorOf(Props[controller.Dispatcher])
}

object Universe extends App {
  val dispatcher = universe.Dispatcher()
  dispatcher.run(universe.Startup.Resume)
}

object RedisReload extends App {
  universe.Builder().build
}

object TestThrift extends App {
  universe.SendRequestToThrift()
}
