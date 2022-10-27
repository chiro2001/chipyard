package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.Config
import freechips.rocketchip.diplomacy.SynchronousCrossing
import freechips.rocketchip.rocket.{DCacheParams, ICacheParams, MulDivParams, RocketCoreParams}
import freechips.rocketchip.subsystem.{CacheBlockBytes, RocketCrossingKey, RocketCrossingParams, RocketTilesKey, SystemBusKey, TileMasterPortParams, WithRV32}
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

class PGL22GOnChipRocketConfig extends Config(
  new testchipip.WithSerialPBusMem ++
    new chipyard.config.WithL2TLBs(0) ++
    new freechips.rocketchip.subsystem.WithNBanks(0) ++
    new freechips.rocketchip.subsystem.WithNoMemPort ++ // remove offchip mem port
    // new freechips.rocketchip.subsystem.WithNBigCores(1) ++
    new WithTinyScratchpadsTinyCore ++             // single tiny rocket-core
    new WithScratchpadsSize(startAddress = 0x90000000L) ++ // use rocket l1 DCache scratchpad as base phys mem
    new WithRV32 ++
    new chipyard.config.AbstractConfig)

class SimPGL22GOnChipRocketConfig extends PGL22GOnChipRocketConfig