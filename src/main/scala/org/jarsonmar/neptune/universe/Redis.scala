package org.jarsonmar.neptune.universe

import redis.clients.jedis._

class Redis(host: String = "localhost", port: Int = 6379) {

  val client = new Jedis(host, port)
  lazy val key_namespace = "org.jarsonmar.neptune"

  def getClient = client

  // key stuff
  def set(key: String, value: String) = client.set(key, value)

  // hash stuff
  def hset(key: String, field: String, value: String) = client.hset(key, field, value)
  def hget(key: String, field: String) = client.hget(key, field)
  def hmget(key: String, fields: String*) = client.hmget(key, fields:_*)

  // set stuff
  def sadd(key: String, value: String) = client.set(key, value)
  def smove(key: String, value: String) = client.set(key, value)
}
