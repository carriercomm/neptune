package org.jarsonmar.neptune.universe

import org.jarsonmar.neptune.thrift
import collection.mutable


class RequestProcessor(redis: Redis) extends thrift.ControllerUpdateService.Iface {

  lazy final val exit_prop_to_key: Map[thrift.ExitProp, String] = Map(
    thrift.ExitProp.NORTH -> "n",
    thrift.ExitProp.SOUTH -> "s",
    thrift.ExitProp.EAST  -> "e",
    thrift.ExitProp.WEST  -> "w",
    thrift.ExitProp.UP    -> "u",
    thrift.ExitProp.DOWN  -> "d"
  )

  lazy final val exit_key_to_prop: Map[String, thrift.ExitProp] = Map(
    "n" -> thrift.ExitProp.NORTH,
    "s" -> thrift.ExitProp.SOUTH,
    "e" -> thrift.ExitProp.EAST,
    "w" -> thrift.ExitProp.WEST,
    "u" -> thrift.ExitProp.UP,
    "d" -> thrift.ExitProp.DOWN
  )

  import collection.JavaConversions._
  def readRequest(req: thrift.LocReadRequest): thrift.LocReadResponse = {

    val res = new thrift.LocReadResponse()
    val locs: mutable.Map[String, thrift.LocReadInstance] = mutable.Map()
    req.id.toArray().foreach { case id: String =>
      val instance = new thrift.LocReadInstance()

      instance.id = id

      var loc_ns = "org.jarsonmar.neptune:loc"

      var prop_ns = List(loc_ns, id, "properties").mkString(":")
      val title = redis.hget(prop_ns, "title")
      val desc = redis.hget(prop_ns, "description")

      var exit_ns = List(loc_ns, id, "exits").mkString(":")

      Option(req.props.get(thrift.LocProp.EXIT)) map { a: java.util.Set[String] =>
        val exit_keys = List.fromArray(a.toArray().map(_.asInstanceOf[String]))
        val exit_values = redis.hmget(exit_ns, exit_keys:_*)
        val exits = exit_keys zip exit_values filter { _._2 != null } map {
          case (k, v) => exit_key_to_prop(k) -> v
        }
        instance.exits = mapAsJavaMap(exits.toMap)
      }
      Option(req.props.get(thrift.LocProp.PROP)) map { a: java.util.Set[String] =>
        val prop_keys = List.fromArray(a.toArray().map(_.asInstanceOf[String]))
        val prop_values = redis.hmget(prop_ns, prop_keys:_*)
        val props = prop_keys zip prop_values filter { _._2 != null }
        instance.props = mapAsJavaMap(props.toMap)
      }

      locs += (id -> instance)
    }
    res.locs = locs

    res
  }
}

