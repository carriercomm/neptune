package org.jarsonmar.neptune.universe

import scala.util.parsing.json._

import scala.collection.mutable

class Builder {
  lazy val redis = new Redis(
    host = "localhost",
    port = 6379
  )

  def build = {
    println("Beginning import...")
    val stream = getClass.getClassLoader.getResourceAsStream("zones.json")
    val json = io.Source.fromInputStream(stream).mkString
    val zone_data = JSON parseRaw json
    zone_data map {
      _.asInstanceOf[JSONObject].obj.toIterator foreach {
        case (key, data_value) => data_value match {
          case JSONArray(l) => l foreach {
            case (e: String) => redis.sadd(key, e)
            case _ => /* nothin */
          }
          case JSONObject(o) => o.toIterator foreach {
            case (hash_field, hash_value: String) => {
              redis.hset(key, hash_field, hash_value)
            }
            case _ => /* nothin */
          }
          case s: String => redis.set(key, s)
        }
      }
    }
    println("Finished import!")
  }
}

object Builder {
  def apply() = new Builder()
}
