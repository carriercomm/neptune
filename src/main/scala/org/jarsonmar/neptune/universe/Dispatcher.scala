package org.jarsonmar.neptune.universe
import akka.actor._
import akka.util._

import org.apache.thrift.TException
import org.apache.thrift.server._
import org.apache.thrift.transport._
import org.apache.thrift.protocol._

import collection.mutable

import org.jarsonmar.neptune.thrift

object Startup {
  sealed trait Build

  case object Fresh extends Build // rebuild redis cache
  case object Resume extends Build // start without rebuilding redis cache
}

class ThriftListener {
  def run(build_type: Startup.Build) = {
    val redis: Redis = new Redis()
    val proc = new RequestProcessor(redis)
    val service_proc = new thrift.LocRequestService.Processor(proc)

    try {
      val serverTransport: TServerTransport = new TServerSocket(9090);
      val server: TServer = new TSimpleServer(new TServer.Args(serverTransport).processor(service_proc));

      build_type match {
        case Startup.Fresh => Builder().build
        case Startup.Resume => /* just start without building */
      }

      server.serve();
    }
    catch {
      case e: Exception => e.printStackTrace();
    }
  }
}

class Nature extends Runnable {
  def run = {
    while(true) {
      Thread.sleep(60 * 1000)
      println("heartbeat - this will be replaced with natural things")
    }
  }
}

class Dispatcher {
  def run(build_type: Startup.Build) = {
    val nature = new Thread(new Nature())
    val thrift_listener = new ThriftListener()

    nature.start() // thread in the background
    thrift_listener.run(Startup.Resume)
  }
}

object Dispatcher {
  def apply() = new Dispatcher()
}

object SendRequestToThrift {
  import collection.JavaConversions._
  def apply() = {
    val transport: TTransport = new TSocket("localhost", 9090);
    transport.open();

    try {
      val protocol: TProtocol = new TBinaryProtocol(transport);
      val client: thrift.LocRequestService.Client = new thrift.LocRequestService.Client(protocol)

      val req: thrift.LocReadRequest = new thrift.LocReadRequest()
      val myid = "start:church"
      req.id = Set(myid)
      req.props = mutable.Map(
        thrift.LocProp.PROP -> setAsJavaSet(Set("title", "description")),
        thrift.LocProp.EXIT -> setAsJavaSet(Set("n"))
      )

      val res: thrift.LocReadResponse = client.readRequest(req)

      Option(res.locs.get(myid)) map { loc =>
        println(loc.props.get("title"))
        println(loc.props.get("description"))
        println()
        println("North exit: " + loc.exits.get(thrift.ExitProp.NORTH))
        if (loc.exits.get(thrift.ExitProp.SOUTH) != null) {
          println("Failed to exclude south exit!")
        }
      } getOrElse { println("Location not found!") }

      transport.close();
    }
    catch {
      case e: TException => e.printStackTrace()
    }
  }
}
