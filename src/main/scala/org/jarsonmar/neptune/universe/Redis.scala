package org.jarsonmar.neptune.universe

import redis.clients.jedis._

class Redis(host: String = "localhost", port: Int = 6379) {
  import collection.JavaConversions._
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
  def sadd(key: String, value: String) = client.sadd(key, value)
  def smove(src_key: String, dst_key: String, value: String) = client.smove(src_key, dst_key, value)
  def smembers(key: String) = client.smembers(key)

  private def locNS(key: String) = key_namespace + ":loc:" + key
  private def objNS(key: String) = key_namespace + ":obj:" + key
  private def mobNS(key: String) = key_namespace + ":mob:" + key
  private def movablesNS         = key_namespace + ":movables"
  private def hostilesNS         = key_namespace + ":hostiles"

  private def locExitsKey(loc: String) = locNS(loc) + ":exits"
  private def locMobsKey(loc: String)  = locNS(loc) + ":mobs"

  private def mobPropKey(mob: String) = mobNS(mob) + ":properties"

  // mobile related commands
  def getFromMobile(key: String) = client.get(mobNS(key))
  def smembersFromMobile(key: String) = client.smembers(mobNS(key))
  def getMovables = asScalaSet(client.smembers(movablesNS))
  def getMobileProperty(mob: String, prop: String) =
    Option(client.hget(mobPropKey(mob), prop))
  def moveMobile(mob: String, src_loc: String, dst_loc: String) = {
    client.smove(mob, locMobsKey(src_loc), locMobsKey(dst_loc))
    client.hset(mobPropKey(mob), "location", dst_loc)
  }

  // location related commands
  def getLocationExits(loc: String) = {
    val exits = List("n", "s", "e", "w", "u", "d")
    val dests = client.hmget(locExitsKey(loc), exits: _*) map { Option(_) }
    (exits zip dests).toMap
  }
}
