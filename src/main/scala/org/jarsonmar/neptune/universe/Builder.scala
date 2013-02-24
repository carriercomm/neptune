package org.jarsonmar.neptune.universe

import scala.util.parsing.json._

import scala.collection.mutable

class Builder {
  lazy val redis = new Redis(
    host = "localhost",
    port = 6379
  )

  private def eachFile(predicate: (String, Option[JSONType]) => Any) = {
    val stream = getClass.getClassLoader.getResourceAsStream("zones/enabled_zones")
    io.Source.fromInputStream(stream).getLines() foreach { zone =>
      val zone_stream = getClass.getClassLoader.getResourceAsStream("zones/" + zone + ".json")
      val json = io.Source.fromInputStream(zone_stream).mkString
      val zone_data = JSON parseRaw json
      predicate(zone, zone_data)
    }
  }

  def build = {

    eachFile { (zone, zone_data) =>
      zone_data map {
        case JSONObject(obj) => obj.get("loc") map {
          _.asInstanceOf[JSONObject].obj.toIterator foreach {
            case (id, data_obj) =>
            val loc_data = data_obj.asInstanceOf[JSONObject].obj
            def loc_data_value(key: String): Any = loc_data.get(key) getOrElse {
              throw new Exception(key + " required") // XXX
            }
            val title = loc_data_value("title").asInstanceOf[String]
            val desc = loc_data_value("description").asInstanceOf[String]

            val exit_data = loc_data.get("exits")
                            .map(_.asInstanceOf[JSONObject].obj)
                            .getOrElse(Map())

            val Array(loc_id, loc_zone) = id.split("@", 2)
            val redis_loc = new Redis.Location(redis, loc_zone, loc_id)
            redis_loc.setTitle(title)
            redis_loc.setDescription(desc)

            for (exit <- List("n", "s", "e", "w", "u", "d")) yield {
              exit_data get exit map {
                exit_loc => redis_loc.setDefaultExit(exit, exit_loc.asInstanceOf[String])
              }
            }
          }
        }
        case _ =>
      }
    }

    redis.getClient.set("foo", "oh hello there my name is jason")
    println(redis.getClient get "foo")
  }
}

object Builder {
  def apply() = new Builder()
}
