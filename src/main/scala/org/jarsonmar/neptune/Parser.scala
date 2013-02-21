package org.jarsonmar.neptune

import akka.actor.IO
import akka.util.ByteString

object Parser {
  import Connection._
  def process(conn: Connection, bytes: ByteString): ByteString = {
    val command: String = bytes.decodeString("UTF8").trim

    val (word:String, leftover: Option[String]) = splitWord(command)

    conn.getState match {
      case ExpectName() =>
        command match {
          case "" => ByteString("Please enter a valid name")
          case _ => {
            conn.setName(command)
            conn.expectGameCommand
            ByteString("Name successfully set. Welcome to the game, " + conn.getName)
          }
        }
        case ExpectGameCommand() => {
          ByteString(processCommand(word, leftover))
        }
    }
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
