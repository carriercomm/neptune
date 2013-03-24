package org.jarsonmar.neptune.controller
import akka.actor._
import akka.util._

import collection.mutable

import org.apache.thrift.TException
import org.apache.thrift.server._
import org.apache.thrift.transport._
import org.apache.thrift.protocol._

import org.jarsonmar.neptune.thrift

class Dispatcher extends Actor {
  private lazy val lineFeed = ByteString(0x0a)

  val state = IO.IterateeRef.Map.async[IO.Handle]()(context.dispatcher)
  val connections: mutable.Map[IO.Handle, Connection] = mutable.Map()

  override def preStart {
    println("starting on port 6715")
    IOManager(context.system).listen("localhost", 6715)
  }

  def receive = {
    case IO.NewClient(server) => {
      val socket = server.accept()
      socket write ByteString("Enter your name: ")
      connections.put(socket, new Connection())
      state(socket) flatMap {_ => processLines(socket)}
    }
    case IO.Read(socket, bytes) => state(socket)(IO.Chunk(bytes))
    case IO.Closed(socket, cause) => {
      state(socket)(IO.EOF)
      state -= socket
    }
  }

  private def processLines(socket: IO.SocketHandle): IO.Iteratee[Unit] = {
    IO.repeat {
      IO.takeUntil(lineFeed).map {
        case line: ByteString => {
          val conn = connections.get(socket).getOrElse(
            throw new Exception("Socket has no associated connection")
          )
          val response = Parser.process(conn, line)
          socket.write(response ++ ByteString("\n> "))
        }
      }
    }
  }
}

class ControllerThrift extends Runnable {
  def run() = {
    println("[controller] Starting thrift server")
    val proc = new NatureUpdateProcessor()
    val service_proc = new thrift.NatureUpdateService.Processor(proc)

    try {
      val serverTransport: TServerTransport = new TServerSocket(9091);
      val server: TServer = new TSimpleServer(new TServer.Args(serverTransport).processor(service_proc));

      server.serve()
    }
    catch {
      case e: Exception => e.printStackTrace();
    }
  }
}
