package org.jarsonmar.neptune.universe

import redis.clients.jedis._

class Redis(host: String = "localhost", port: Int = 6379) {

  val client = new Jedis(host, port)
  lazy val key_namespace = "org.jarsonmar.neptune"

  def getClient = client

  // internal method
  private def ns(key: String) = key_namespace + ":" + key

  // key stuff
  def set(key: String, value: String) = client.set(ns(key), value)

  // hash stuff
  def hset(key: String, field: String, value: String) = client.hset(ns(key), field, value)
  def hget(key: String, field: String) = client.hget(ns(key), field)

  // set stuff
  def sadd(key: String, value: String) = client.set(ns(key), value)
  def smove(key: String, value: String) = client.set(ns(key), value)
}

object Redis {
  class Location(redis: Redis, loc_zone: String, loc_id: String) {
    private def ns(key: String) = loc_zone + ":" + loc_id + ":" + key

    def setTitle(title: String) = redis.hset(ns("properties"), "title", title)
    def getTitle(title: String): String = redis.hget(ns("properties"), "title")

    def setDescription(title: String) = redis.hset(ns("properties"), "description", title)
    def getDescription(title: String) = redis.hget(ns("properties"), "description")

    def setDefaultExit(exit: String,
      exit_loc: String) = redis.hset(ns("exits"), exit, exit_loc)
    def getDefaultExit(exit: String): String = redis.hget(ns("defexits"), exit)
    def setCurrentExit(exit: String,
      exit_loc_type: String,
      exit_loc_data: String) = {
      redis.hset(ns("exits"), exit, exit_loc_type + ":" + exit_loc_data)
    }
  }

  class Player(redis: Redis) {
    // TODO use this to abstract away set logic for players
  }
}
