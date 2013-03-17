package org.jarsonmar.neptune.universe
import akka.actor._
import akka.util._

import org.apache.thrift.TException
import org.apache.thrift.server._
import org.apache.thrift.transport._
import org.apache.thrift.protocol._

import collection.mutable
import concurrent.duration._

import java.util.Random

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
case class RandomTransfer(mob_key: String)
case class TransferComplete(mob: String, src_loc: String, dst_loc: String)

object Nature {
  def tickMean = 2000.0
  def tickStddev = 1000.0
}

// uses redis-dispatcher config
class RedisIO(redis: Redis) extends Actor {
  import context.dispatcher
  private val rand = new Random(System.currentTimeMillis())

  def receive = {
    case RandomTransfer(mob_key) => {
      redis.getMobileProperty(mob_key, "location") map { (mob_loc) =>
        val exits = redis.getLocationExits(mob_loc)
        val available_dests = exits.values.collect({case Some(x) => x}).toList

        if (available_dests.length > 0) {
          val rand_idx = rand.nextInt(available_dests.length)
          val dest = available_dests(rand_idx)
          redis.moveMobile(mob_key, mob_loc, dest)
          context.actorFor("/user/nature") ! TransferComplete(mob_key, mob_loc, dest)
        }
      }
    }
  }
}

// uses default-dispatcher config
class Nature extends Actor {
  import context.dispatcher
  def receive = {
    case NatureTick => /* don't do anything here yet */
    case MoveMobile(mob_key, speed_ref, start) => {
      if (!start) {
        context.actorFor("/user/redis_io") ! RandomTransfer(mob_key)
      }
      val randOffset = Nature.tickStddev * new util.Random().nextGaussian()
      val period = Nature.tickMean / (speed_ref / 100.0)
      val tick = period + randOffset
      context.system.scheduler.scheduleOnce(
        (if (tick >= 0.0) tick else 0.0) milliseconds,
        context.self,
        MoveMobile(mob_key, speed_ref, false)
      )
    }
    case TransferComplete(mob, src_loc, dst_loc) => {
      //TODO thrift dispatcher
      //println("transfer: " + List(mob, src_loc, dst_loc).toString)
    }
  }
}

class NatureThread extends Runnable {
  def run = {
    val system = ActorSystem.create()

    import system.dispatcher

    val redis: Redis = new Redis()

    val nature_ref = system.actorOf(Props[Nature], "nature")
    val redis_io_ref = system.actorOf(
      Props(new RedisIO(redis)).withDispatcher("akka.actor.redis-dispatcher"),
      "redis_io"
    )

    redis.getMovables foreach { (m) =>
      redis.getMobileProperty(m, "speed") map { (value) =>
        nature_ref ! MoveMobile(m, value.toDouble)
      }
    }

    while(true) {
      Thread.sleep(1000)
      nature_ref ! NatureTick
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
