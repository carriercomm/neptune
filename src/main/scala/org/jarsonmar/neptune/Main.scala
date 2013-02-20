package org.jarsonmar.neptune

import akka.actor._
import akka.util._

object Neptune {
  class Listener extends Actor {

    val state = IO.IterateeRef.Map.async[IO.Handle]()(context.dispatcher)

    override def preStart {
      println("starting on port 6715")
      IOManager(context.system).listen("localhost", 6715)
    }

    def receive = {
      case IO.NewClient(server) => {
        val socket = server.accept()
        socket write ByteString("> ")
        val conn = new Connection(socket)
        state(socket).flatMap(_ => conn.processLines)
      }
      case IO.Read(socket, bytes) => state(socket)(IO.Chunk(bytes))
      case IO.Closed(socket, cause) => {
        state(socket)(IO.EOF)
        state -= socket
      }
    }
  }
}

object Main extends App {
  val system = ActorSystem("Neptune")
  val server = system.actorOf(Props[Neptune.Listener])
}
