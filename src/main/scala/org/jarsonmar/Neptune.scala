package org.jarsonmar

import java.io._

import akka.actor.Actor
import akka.actor.Props
//import akka.pattern.ask
//import akka.util.duration._
//import akka.util.Timeout
//
//import akka.io.IO
//import akka.io.Tcp._


//case object Tick
//case object Get

object Neptune extends App {
  println("Hello world")
}
//object Neptune extends App {
//  val system = ActorSystem("Neptune")
//  val server = system.actorOf(Props[Neptune.Server])
//
//  val listener = new ServerSocket(6715);
//  server ! Bind(socket, "localhost")
//
//  //
//  //  case count => println("Count is " + count)
//  //}
//
//  system.shutdown()
//
//  class Server extends IO(Tcp) {
//    var count = 0
//
//    def receive = {
//      case Tick => count += 1
//      case Get  => sender ! count
//    }
//  }
//}
