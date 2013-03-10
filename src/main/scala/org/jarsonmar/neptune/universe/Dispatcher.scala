package org.jarsonmar.neptune.universe
import akka.actor._
import akka.util._

import org.apache.thrift.TException
import org.apache.thrift.server._
import org.apache.thrift.transport._
import org.apache.thrift.protocol._

import collection.mutable
import concurrent.duration._

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

case object NatureTick
case class MoveMobile(mob_key: String, speed_ref: Double, start: Boolean = true)

object Nature {
  def tickMean = 2000.0
  def tickStddev = 1000.0
}

class Nature extends Actor {
  import context.dispatcher
  def receive = {
    case NatureTick => /* don't do anything here yet */
    case MoveMobile(mob_key, speed_ref, start) => {
      if (!start) {
        println("TODO: move mobile " + mob_key)
      }
      val randOffset = Nature.tickStddev * new util.Random().nextGaussian()
      val period = Nature.tickMean / (speed_ref / 100.0)
      val tick = period + randOffset
      context.system.scheduler.scheduleOnce(
        (if (tick >= 0.0) tick else 0.0) milliseconds,
        context.system.actorOf(Props(this)),
        MoveMobile(mob_key, speed_ref, false)
      )
    }
  }
}

class NatureThread extends Runnable {
  def run = {
    val system = ActorSystem("Nature")
    import system.dispatcher

    val redis: Redis = new Redis()

    val nature = system.actorOf(Props[Nature])

    redis.getMovables foreach { (m) =>
      redis.getMobileProperty(m, "speed") map { (value) =>
        nature ! MoveMobile(m, value.toDouble)
      }
    }

    while(true) {
      Thread.sleep(1000)
      nature ! NatureTick
    }
  }
}

class Dispatcher {
  def run(build_type: Startup.Build) = {
    val nature = new Thread(new NatureThread())
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
