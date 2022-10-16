// See LICENSE for license details.
package chipyard.fpga.pgl22g

import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy.{DTSModel, DTSTimebase}
import freechips.rocketchip.system._
import freechips.rocketchip.tile._
import sifive.blocks.devices.uart._
import testchipip.SerialTLKey
import chipyard.BuildSystem
import chipyard.config.WithL2TLBs

class WithDefaultPeripherals extends Config((site, here, up) => {
  case PeripheryUARTKey => List(
    UARTParams(address = 0x10013000))
  case DTSTimebase => BigInt(32768)
  case JtagDTMKey => new JtagDTMConfig(
    idcodeVersion = 2,
    idcodePartNum = 0x000,
    idcodeManufId = 0x489,
    debugIdleCycles = 5)
  case SerialTLKey => None // remove serialized tl port
})

class PGL22GRocketConfig extends Config(
  new chipyard.config.WithTLSerialLocation(FBUS, PBUS) ++ // attach TL serial adapter to f/p busses
    new WithIncoherentBusTopology ++ // use incoherent bus topology
    new WithNBanks(0) ++ // remove L2$
    new WithL1ICacheWays(1) ++
    new WithL1ICacheSets(64) ++
    new WithL1DCacheWays(1) ++
    new WithL1DCacheSets(64) ++
    new WithoutFPU ++
    new WithoutMulDiv ++
    new WithNoMemPort ++ // remove backing memory
    new With1TinyCore ++ // single tiny rocket-core
    new WithRV32 ++ // set RocketTiles to be 32-bit
    new chipyard.config.AbstractConfig)

// DOC include start: AbstractPGL22G and Rocket
class WithPGL22GTweaks extends Config(
  new WithPGL22GJTAGHarnessBinder ++
    new WithPGL22GUARTHarnessBinder ++
    new WithPGL22GResetHarnessBinder ++
    new WithDebugResetPassthrough ++
    new WithDefaultPeripherals ++
    new freechips.rocketchip.subsystem.WithoutTLMonitors ++
    new freechips.rocketchip.subsystem.WithNBreakpoints(2))

class TinyRocketPGL22GConfig extends Config(
  new WithPGL22GTweaks ++
    new PGL22GRocketConfig)
// DOC include end: AbstractPGL22G and Rocket
