package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.Config
import chipyard.config.{WithL2TLBs, WithSerialTLBackingMemory}
import freechips.rocketchip.diplomacy.SynchronousCrossing
import freechips.rocketchip.rocket.{DCacheParams, ICacheParams, MulDivParams, RocketCoreParams}
import freechips.rocketchip.subsystem.{CacheBlockBytes, RocketCrossingKey, RocketCrossingParams, RocketTilesKey, SystemBusKey, TileMasterPortParams, WithNSmallCores, WithRV32, WithoutFPU}
import freechips.rocketchip.tile.{RocketTileParams, XLen}

class WithTinyScratchpadsTinyCore extends Config((site, here, up) => {
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
      scratch = Some(0x80000000L))),
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

class PGL22GOnChipBaseConfig extends Config(
  new testchipip.WithSerialPBusMem ++
    new chipyard.config.WithL2TLBs(0) ++
    new freechips.rocketchip.subsystem.WithNBanks(0) ++
    new freechips.rocketchip.subsystem.WithNoMemPort // remove offchip mem port
)

class PGL22GOnChipRocketConfig extends Config(
  // new PGL22GOnChipBaseConfig ++
  new WithRV32 ++
    new chipyard.config.WithRocketICacheScratchpad ++ // use rocket ICache scratchpad
    new chipyard.config.WithRocketDCacheScratchpad ++ // use rocket DCache scratchpad
    // new WithTinyScratchpadsTinyCore ++ // single tiny rocket-core
    new freechips.rocketchip.subsystem.WithNBigCores(1) ++
    new WithBareCoreMarkBootROM ++
    new chipyard.config.AbstractConfig)

class PGL22GOnChipRocketCoreMarkConfig extends Config(
  new PGL22GOnChipBaseConfig ++
    // new freechips.rocketchip.subsystem.WithNBigCores(1) ++
    // new WithScratchpadsSize(startAddress = 0x90000000L, sizeKB = 64) ++ // use rocket l1 DCache scratchpad as base phys mem
    new WithScratchpadsSize(startAddress = 0x80000000L, sizeKB = 64) ++ // use rocket l1 DCache scratchpad as base phys mem
    // new WithBareCoreMarkBootROM(address = 0x80000000L, hang = 0x80000000L) ++
    new WithBareCoreMarkBootROM ++
    new WithTinyScratchpadsTinyCore ++ // single tiny rocket-core
    new ModifiedAbstractConfig)

class PGL22GOnChipRocketTestsBaseConfig extends Config(
  new PGL22GOnChipBaseConfig ++
    new WithScratchpadsSize(startAddress = 0x80000000L, sizeKB = 16) ++ // use rocket l1 DCache scratchpad as base phys mem
    // new WithBareCoreMarkBootROM(address = 0x80000000L, hang = 0x80000000L) ++
    // new WithBareCoreMarkBootROM ++
    new WithTestsBootROM ++
    // new chipyard.config.WithNoDebug ++ // remove debug module
    // new freechips.rocketchip.subsystem.WithoutTLMonitors ++
    new WithoutFPU ++
    new WithRV32 ++
    new freechips.rocketchip.subsystem.WithNBreakpoints(2) ++
    new freechips.rocketchip.subsystem.WithNoMemPort ++ // remove offchip mem port
    new chipyard.harness.WithSimSerial ++
    new ModifiedAbstractConfig)

class PGL22GOnChipRocketTestsConfig extends Config(
  new WithTinyScratchpadsTinyCore ++ // single tiny rocket-core
    new PGL22GOnChipRocketTestsBaseConfig
)

class PGL22GOnChipRocketTestsSmallConfig extends Config(
  new WithNSmallCores(1) ++
    new SimPGL22GOnChipRocketBaseConfig
)

class SimPGL22GOnChipRocketBaseConfig extends Config(
  new PGL22GOnChipBaseConfig ++
    // new freechips.rocketchip.subsystem.WithNBigCores(1) ++
    // new WithScratchpadsSize(startAddress = 0x90000000L, sizeKB = 64) ++ // use rocket l1 DCache scratchpad as base phys mem
    new WithScratchpadsSize(startAddress = 0x80000000L, sizeKB = 64) ++ // use rocket l1 DCache scratchpad as base phys mem
    // new WithBareCoreMarkBootROM(address = 0x80000000L, hang = 0x80000000L) ++
    // new WithBareCoreMarkBootROM ++
    new WithTestsBootROM ++
    new WithRV32 ++
    new chipyard.config.AbstractConfig)

class SimPGL22GOnChipRocketConfig extends Config(
  new SimPGL22GOnChipRocketBaseConfig ++
    new WithTinyScratchpadsTinyCore
)

class SimPGL22GOnChipRocketSmallConfig extends Config(
  // new WithNSmallCores(1) ++
  new SimPGL22GOnChipRocketBaseConfig ++
    // new chipyard.config.WithRocketICacheScratchpad ++ // use rocket ICache scratchpad
    // new chipyard.config.WithRocketDCacheScratchpad ++ // use rocket DCache scratchpad
    new freechips.rocketchip.subsystem.WithNBigCores(1) ++
    new chipyard.config.AbstractConfig
)