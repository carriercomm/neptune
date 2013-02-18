package org.jarsonmar.neptune

import akka.actor.IO
import akka.util.ByteString

object Parser {
  def process(bytes: ByteString): ByteString = {
    val command: String = bytes.decodeString("UTF8")

    val (word:String, leftover: Option[String]) = splitWord(command)

    // TODO command stuff doesn't really apply here
    //      need input state for name, entry, etc.
    ByteString(processCommand(word, leftover) + "\n")
  }

  // TODO move into its own thing
  def processCommand(command: String, leftover: Option[String]): String = command match {
    case "look" => "You look over there"
    case "go" => leftover match {
      case Some("north") => "You head north"
      case Some("south") => "You head south"
      case Some("east")  => "You head east"
      case Some("west")  => "You head west"
      case Some(_)       => "Unfamiliar with that direction"
      case None          => "You go in a random direction"
    }
    case "" => ""
    case _  => "Unknown command"
  }

  private def splitWord(command: String): (String, Option[String]) = {
    val data: Array[String] = command.trim().split("\\s+", 2)

    (data(0).toLowerCase(), if (data.length > 1) Some(data(1)) else None)
  }
}
