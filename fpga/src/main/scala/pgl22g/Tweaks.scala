package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.Config
import chipyard.config.WithL2TLBs
import freechips.rocketchip.subsystem.{WithL1DCacheSets, WithL1ICacheSets, WithRV32, WithoutFPU}

class WithPGL22GTweaks(freq: Double = 8) extends Config(
  // harness binders
  new WithUART ++
    // new WithSPISDCard ++
    // io binders
    new WithUARTIOPassthrough ++
    // new WithSPIIOPassthrough ++
    new WithDefaultPeripherals ++
    new WithDefaultTimebase ++
    new WithSystemModifications ++ // setup busses, use sdboot bootrom, setup ext. mem. size
    new chipyard.config.WithNoDebug ++ // remove debug module
    new freechips.rocketchip.subsystem.WithoutTLMonitors ++
    new freechips.rocketchip.subsystem.WithNBreakpoints(2) ++
    new freechips.rocketchip.subsystem.WithNMemoryChannels(1) ++
    new WithFPGAFrequency(freq) ++
    new WithoutFPU ++
    new WithL2TLBs(0) ++
    new WithL1ICacheSets(64 * 4) ++
    new WithL1DCacheSets(64 * 4) ++
    new WithRV32 // set RocketTiles to be 32-bit
)

class WithPGL22GPerfTweaks(freq: Double = 8) extends Config(
  // harness binders
  new WithPerfUART ++
    // new WithSPISDCard ++
    // io binders
    // new WithUARTIOPassthrough ++
    // new WithSPIIOPassthrough ++
    new WithDefaultPeripherals ++
    new WithSystemModifications ++ // setup busses, use sdboot bootrom, setup ext. mem. size
    new chipyard.config.WithNoDebug ++ // remove debug module
    new freechips.rocketchip.subsystem.WithoutTLMonitors ++
    new freechips.rocketchip.subsystem.WithNBreakpoints(2) ++
    new freechips.rocketchip.subsystem.WithNMemoryChannels(1) ++
    new WithFPGAFrequency(freq) ++
    new WithoutFPU ++
    new WithL2TLBs(0) ++
    new WithL1ICacheSets(64 * 4) ++
    new WithL1DCacheSets(64 * 4) ++
    new WithRV32 // set RocketTiles to be 32-bit
)

class WithPGL22GSimTweaks extends Config(
  new freechips.rocketchip.subsystem.WithoutTLMonitors ++
    new freechips.rocketchip.subsystem.WithNBreakpoints(2) ++
    new WithoutFPU ++
    new WithL2TLBs(0) ++
    new WithL1ICacheSets(64 * 4) ++
    new WithL1DCacheSets(64 * 4) ++
    // Total 48 Kbit
    new freechips.rocketchip.subsystem.WithInclusiveCache(nWays = 2, capacityKB = 32, outerLatencyCycles = 3, subBankingFactor = 2) ++
    new WithRV32 // set RocketTiles to be 32-bit
)

class WithPGL22GSimTinyTweaks extends Config(
  new chipyard.config.WithNoDebug ++ // remove debug module
    new freechips.rocketchip.subsystem.WithoutTLMonitors ++
    new freechips.rocketchip.subsystem.WithNBreakpoints(2) ++
    new chipyard.harness.WithBlackBoxSimMem ++ // add SimDRAM DRAM model for axi4 backing memory, if axi4 mem is enabled
    new chipyard.harness.WithSimSerial ++ // add external serial-adapter and RAM
    new chipyard.harness.WithSimDebug ++ // add SimJTAG or SimDTM adapters if debug module is enabled
    new WithRV32
)

