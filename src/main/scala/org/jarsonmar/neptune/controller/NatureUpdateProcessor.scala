package org.jarsonmar.neptune.controller

import org.jarsonmar.neptune.thrift

class NatureUpdateProcessor extends thrift.NatureUpdateService.Iface {
  def mobileMovement(movement: thrift.MobileMovement): Boolean = {
    println(movement.mob + " moved somewhere")

    true
  }
}
