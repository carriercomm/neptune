package org.jarsonmar.neptune

import akka.actor._
import akka.util._


case class CommandEntry(line: ByteString)

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
        state(socket).flatMap(_ => Listener.processLines(socket))
      }
      case IO.Read(socket, bytes) => state(socket)(IO.Chunk(bytes))
      case IO.Closed(socket, cause) => {
        state(socket)(IO.EOF)
        state -= socket
      }
    }
  }

  object Listener {
    val lineFeed = ByteString(0x0a) // linefeed

    def processLines(socket: IO.SocketHandle): IO.Iteratee[Unit] = {
      IO.repeat {
        IO.takeUntil(lineFeed).map {
          case line: ByteString => socket.write(ByteString("This will be parsed: ") ++ line ++ lineFeed)
        }
      }
    }
  }
}

object Main extends App {
  val system = ActorSystem("Neptune")
  val server = system.actorOf(Props[Neptune.Listener])
}
