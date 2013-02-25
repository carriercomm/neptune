package org.jarsonmar.neptune.universe

import scala.util.parsing.json._

import scala.collection.mutable

class Builder {
  lazy val redis = new Redis(
    host = "localhost",
    port = 6379
  )

  def build = {

    val stream = getClass.getClassLoader.getResourceAsStream("zones.json")
    val json = io.Source.fromInputStream(stream).mkString
    val zone_data = JSON parseRaw json
    zone_data map {
      _.asInstanceOf[JSONObject].obj.toIterator foreach {
        case (key, data_value) => data_value match {
          case JSONArray(l) => l foreach { e: Any =>
            if (e.isInstanceOf[String]) { // would be null otherwise
              redis.sadd(key, e.asInstanceOf[String])
            }
          }
          case JSONObject(o) => o.toIterator foreach {
            case (hash_field, hash_value) => {
              if (hash_value.isInstanceOf[String]) {
                redis.hset(key, hash_field, hash_value.asInstanceOf[String])
              }
            }
          }
          case s: String => redis.set(key, s)
        }
      }
    }
  }
}

object Builder {
  def apply() = new Builder()
}
