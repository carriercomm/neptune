package org.jarsonmar

import akka.actor._
import akka.util._


//case object Tick
//case object Get

object Neptune {
  class Listener extends Actor {

    override def preStart {
      println("starting on port 6715")
      IOManager(context.system).listen("localhost", 6715)
    }

    def receive = {
      case IO.NewClient(socket) =>
        println("aaaaaaaaaaaaaaaaahhhhhhhhhhhhhh")
    }
  }
}

object Main extends App {
  val system = ActorSystem("Neptune")
  val server = system.actorOf(Props[Neptune.Listener])
}
