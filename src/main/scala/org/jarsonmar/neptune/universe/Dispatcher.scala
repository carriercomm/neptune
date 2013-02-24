package org.jarsonmar.neptune.universe
import akka.actor._
import akka.util._

import org.apache.thrift.TException
import org.apache.thrift.server._
import org.apache.thrift.transport._
import org.apache.thrift.protocol._

import org.jarsonmar.neptune.thrift

class Dispatcher {
  def run = {
    val proc = new RequestProcessor()
    val service_proc = new thrift.RequestService.Processor(proc)

    try {
      val serverTransport: TServerTransport = new TServerSocket(9090);
      val server: TServer = new TSimpleServer(new TServer.Args(serverTransport).processor(service_proc));

      Builder().build

      server.serve();
    }
    catch {
      case e: Exception => e.printStackTrace();
    }
  }
}

object Dispatcher {
  def apply() = new Dispatcher()
}

object SendRequestToThrift {
  def apply() = {
    val transport: TTransport = new TSocket("localhost", 9090);
    transport.open();

    try {
      val protocol: TProtocol = new TBinaryProtocol(transport);
      val client: thrift.RequestService.Client = new thrift.RequestService.Client(protocol)

      val req: thrift.Request = new thrift.Request()
      req.rtype = thrift.RequestType.READ
      client.processRequest(req)

      transport.close();
    }
    catch {
      case e: TException => e.printStackTrace()
    }
  }
}
