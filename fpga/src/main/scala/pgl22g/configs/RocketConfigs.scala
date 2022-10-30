package pgl22g.configs

import chipsalliance.rocketchip.config.Config
import freechips.rocketchip.devices.debug.DebugModuleKey
import freechips.rocketchip.diplomacy.SynchronousCrossing
import freechips.rocketchip.rocket.{DCacheParams, ICacheParams, MulDivParams, RocketCoreParams}
import freechips.rocketchip.subsystem.{CacheBlockBytes, RocketCrossingKey, RocketCrossingParams, RocketTilesKey, SystemBusKey, TileMasterPortParams, WithBufferlessBroadcastHub, WithNMemoryChannels, WithRV32}
import freechips.rocketchip.tile.{RocketTileParams, XLen}
import pgl22g._

class RocketSmall32Config extends Config(
  new freechips.rocketchip.subsystem.WithNSmallCores(1) ++
    new WithRV32 ++
    new chipyard.config.AbstractConfig)

class RocketBig32Config extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++
    new WithRV32 ++
    new chipyard.config.AbstractConfig)

class WithPGL22GRocketCore extends Config((site, here, up) => {
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
  new WithPGL22GRocketCore ++ // single tiny rocket-core
    // new WithNSmallCores(1) ++
    new ModifiedAbstractConfig)

class WithNoDebug extends Config((site, here, up) => {
  case DebugModuleKey => None
})

class TinyRocketPGL22GConfig extends Config(
  new WithPGL22GTweaks ++
    new WithPGL22GTLMem ++
    new PGL22GRocketConfig
  // new chipyard.RocketConfig
)

class PGL22GAXIMemConfig extends Config(
  new WithPGL22GTweaks ++
    new WithPGL22GAXIMem ++
    new PGL22GRocketConfig
  // new chipyard.RocketConfig
)

class PGL22GBareConfig extends Config(
  new WithPGL22GTweaks ++
    new WithPGL22GAXIMem ++
    new PGL22GRocketConfig
  // new chipyard.RocketConfig
)

class PGL22GConfig extends Config(
  new WithPGL22GTweaks ++
    new WithPGL22GTLMem ++
    new PGL22GRocketConfig
  // new chipyard.RocketConfig
)

class PGL22GPerfConfig extends Config(
  // // new WithPGL22GPerfTweaks ++
  // //   new WithNBigCores(1) ++
  // new WithNSmallCores(1) ++
  //   // new MemPortOnlyConfig ++
  //   new WithNoDebug ++
  //   new WithoutTLMonitors ++
  //   new WithDefaultPeripherals ++
  //   new WithDefaultTimebase ++
  //   new WithPGL22GPerfTweaks ++
  //   new BasePerfConfig

  new WithPGL22GPerfTweaks ++
    // new WithPGL22GAXIMem ++
    // new WithBroadcastManager ++
    // new WithNTrackersPerBank(1) ++
    // new WithBlackBoxDDRMem ++
    new WithPGL22GAXIMem ++
    new WithNMemoryChannels(1) ++
    new WithBufferlessBroadcastHub ++
    // new testchipip.WithRingSystemBus ++
    // new WithAXIIOPassthrough ++
    new PGL22GRocketConfig
)

class PGL22GTinyConfig extends Config(
  new WithPGL22GPerfTweaks ++
    new WithPGL22GAXIMem ++
    new WithNMemoryChannels(1) ++
    new WithBufferlessBroadcastHub ++
    // new PGL22GRocketConfig ++
    // new chipyard.config.WithTLSerialLocation(
    //   freechips.rocketchip.subsystem.FBUS,
    //   freechips.rocketchip.subsystem.PBUS) ++ // attach TL serial adapter to f/p busses
    // new freechips.rocketchip.subsystem.WithIncoherentBusTopology ++ // use incoherent bus topology
    // new freechips.rocketchip.subsystem.WithNBanks(0) ++ // remove L2$
    new freechips.rocketchip.subsystem.WithNoMemPort ++ // remove backing memory
    // new freechips.rocketchip.subsystem.With1TinyCore ++ // single tiny rocket-core
    new WithPGL22GRocketCore ++
    new PGL22GRocketConfig
)

class SimTinyRocketPGL22GConfig extends Config(
  new WithPGL22GSimTweaks ++
    new WithPGL22GSimTinyTweaks ++
    new PGL22GRocketConfig
)