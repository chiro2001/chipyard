// See LICENSE for license details.
package chipyard.fpga.pgl22g

import chipyard.ExtTLMem
import chipyard.config.{AbstractConfig, WithL2TLBs}
import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.diplomacy.{DTSTimebase, SynchronousCrossing}
import freechips.rocketchip.rocket.{DCacheParams, ICacheParams, MulDivParams, RocketCoreParams}
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tile.{RocketTileParams, XLen}
import sifive.blocks.devices.uart._
import sifive.fpgashells.shell.pango.PGL22GDDRSize
import testchipip.SerialTLKey

class WithDefaultPeripherals extends Config((site, here, up) => {
  // case PeripheryUARTKey => List(
  //   UARTParams(address = 0x10013000))
  // case DTSTimebase => BigInt(32768)
  // case JtagDTMKey => new JtagDTMConfig(
  //   idcodeVersion = 2,
  //   idcodePartNum = 0x000,
  //   idcodeManufId = 0x489,
  //   debugIdleCycles = 5)
  // case SerialTLKey => None // remove serialized tl port
  case PeripheryUARTKey => List(UARTParams(address = BigInt(0x64000000L)))
  // case PeripherySPIKey => List(SPIParams(rAddress = BigInt(0x64001000L)))
  // case VCU118ShellPMOD => "SDIO"
})

class WithSystemModifications extends Config((site, here, up) => {
  case DTSTimebase => BigInt((1e6).toLong)
  // case BootROMLocated(x) => up(BootROMLocated(x), site).map { p =>
  //   // invoke makefile for sdboot
  //   val freqMHz = (site(DefaultClockFrequencyKey) * 1e6).toLong
  //   val make = s"make -C fpga/src/main/resources/vcu118/sdboot PBUS_CLK=${freqMHz} bin"
  //   require (make.! == 0, "Failed to build bootrom")
  //   p.copy(hang = 0x10000, contentFileName = s"./fpga/src/main/resources/vcu118/sdboot/build/sdboot.bin")
  // }
  case ExtMem => up(ExtMem, site).map(x => x.copy(master = x.master.copy(size = site(PGL22GDDRSize)))) // set extmem to DDR size
  case SerialTLKey => None // remove serialized tl port
})

class WithFPGAFrequency(fMHz: Double) extends Config(
  new chipyard.config.WithPeripheryBusFrequency(fMHz) ++ // assumes using PBUS as default freq.
    new chipyard.config.WithMemoryBusFrequency(fMHz)
)

class WithPGL22GTinyCore extends Config((site, here, up) => {
  case XLen => 32
  case RocketTilesKey => List(RocketTileParams(
    core = RocketCoreParams(
      useVM = false,
      fpu = None,
      mulDiv = Some(MulDivParams(mulUnroll = 8))),
    btb = None,
    dcache = Some(DCacheParams(
      rowBits = site(SystemBusKey).beatBits,
      nSets = 256, // 16Kb scratchpad
      nWays = 1,
      nTLBSets = 1,
      nTLBWays = 4,
      nMSHRs = 0,
      blockBytes = site(CacheBlockBytes),
      // scratch = Some(0x80000000L)
    )),
    icache = Some(ICacheParams(
      rowBits = site(SystemBusKey).beatBits,
      nSets = 64,
      nWays = 1,
      nTLBSets = 1,
      nTLBWays = 4,
      blockBytes = site(CacheBlockBytes)))))
  case RocketCrossingKey => List(RocketCrossingParams(
    crossingType = SynchronousCrossing(),
    master = TileMasterPortParams()
  ))
})

class PGL22GRocketConfig extends Config(
  // new chipyard.config.WithTLSerialLocation(FBUS, PBUS) ++ // attach TL serial adapter to f/p busses
  //   new WithIncoherentBusTopology ++ // use incoherent bus topology
  //   new WithNBanks(0) ++ // remove L2$
  // new WithJustOneBus ++
  new WithPGL22GTinyCore ++ // single tiny rocket-core
    // new WithNSmallCores(1) ++
    new AbstractConfig)

class WithNoDebug extends Config((site, here, up) => {
  case DebugModuleKey => None
})

// class WithMemory extends Config((site, here, up) => {
//   // case ExtMem => None // disable AXI backing memory
//   case ExtTLMem => None
// })

// DOC include start: AbstractPGL22G and Rocket
class WithPGL22GTweaks extends Config(
  // harness binders
  new WithUART ++
    // new WithSPISDCard ++
    new WithDDRMem ++
    // io binders
    new WithUARTIOPassthrough ++
    // new WithSPIIOPassthrough ++
    new WithTLIOPassthrough ++
    new WithDefaultPeripherals ++
    new chipyard.config.WithTLBackingMemory ++ // use TL backing memory
    new WithSystemModifications ++ // setup busses, use sdboot bootrom, setup ext. mem. size
    new chipyard.config.WithNoDebug ++ // remove debug module
    new freechips.rocketchip.subsystem.WithoutTLMonitors ++
    new freechips.rocketchip.subsystem.WithNBreakpoints(2) ++
    new freechips.rocketchip.subsystem.WithNMemoryChannels(1) ++
    new WithFPGAFrequency(8) ++
    new WithoutFPU ++
    new WithL2TLBs(0) ++
    // new WithNBanks(0) ++
    new WithL1ICacheSets(64 * 4) ++
    new WithL1DCacheSets(64 * 4) ++
    // new freechips.rocketchip.subsystem.WithInclusiveCache(nWays = 2, capacityKB = 16) ++
    // Total 48 Kbit
    new freechips.rocketchip.subsystem.WithInclusiveCache(nWays = 2, capacityKB = 32, outerLatencyCycles = 3, subBankingFactor = 2) ++
    new WithRV32 // set RocketTiles to be 32-bit
)

class TinyRocketPGL22GConfig extends Config(
  new WithPGL22GTweaks ++
    new PGL22GRocketConfig
  // new chipyard.RocketConfig
)
// DOC include end: AbstractPGL22G and Rocket

class SimTinyRocketPGL22GConfig extends Config(
  new PGL22GRocketConfig
)
// class SimTinyRocketPGL22GConfig extends Config(
//   new freechips.rocketchip.subsystem.WithNBigCores(1) ++         // single rocket-core
//     new chipyard.config.AbstractConfig)