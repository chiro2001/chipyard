package chipyard.fpga.pgl22g.bringup

import chisel3._

import freechips.rocketchip.subsystem._
import freechips.rocketchip.system._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._

import chipyard.{DigitalTop, DigitalTopModule}

// ------------------------------------
// Bringup PGL22G DigitalTop
// ------------------------------------

class BringupPGL22GDigitalTop(implicit p: Parameters) extends DigitalTop
  with sifive.blocks.devices.i2c.HasPeripheryI2C
  with testchipip.HasPeripheryTSIHostWidget
{
  override lazy val module = new BringupPGL22GDigitalTopModule(this)
}

class BringupPGL22GDigitalTopModule[+L <: BringupPGL22GDigitalTop](l: L) extends DigitalTopModule(l)
  with sifive.blocks.devices.i2c.HasPeripheryI2CModuleImp
