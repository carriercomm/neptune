package org.jarsonmar.neptune.universe

import org.jarsonmar.neptune.thrift

class RequestProcessor extends thrift.RequestService.Iface {
  def processRequest(req: thrift.Request) = println("this will process the request")
}

