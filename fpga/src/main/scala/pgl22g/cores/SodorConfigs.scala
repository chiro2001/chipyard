package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.Config
import chipyard.config.WithL2TLBs
import freechips.rocketchip.subsystem.{WithBufferlessBroadcastHub, WithL1DCacheSets, WithL1ICacheSets, WithNMemoryChannels, WithoutFPU}

class PGL22GSodorConfigBase extends Config(
  new WithPGL22GTweaks ++
    new WithPGL22GAXIMem(base = BigInt(0x80000000L)) ++
    // new testchipip.WithSerialTLWidth(32) ++
    // new testchipip.WithSerialPBusMem ++
    // new WithSerialTLMem ++
    // new WithTLIOPassthrough ++
    // new freechips.rocketchip.subsystem.WithScratchpadsOnly ++ // use sodor tile-internal scratchpad
    // new WithSmallScratchpadsOnly ++
    // new freechips.rocketchip.subsystem.WithNoMemPort ++ // use no external memory
    new WithoutFPU ++
    new WithL2TLBs(0) ++
    new WithL1ICacheSets(64 * 2) ++
    new WithL1DCacheSets(64 * 2) ++
    // new WithDefaultMemPort ++
    // new freechips.rocketchip.subsystem.WithNBanks(0) ++
    // new testchipip.WithRingSystemBus ++
    new WithNMemoryChannels(1) ++
    new WithBufferlessBroadcastHub ++
    new ModifiedAbstractConfig
)

class PGL22GSodorConfig extends Config(
  new sodor.common.WithNSodorCores(1, internalTile = sodor.common.Stage5Factory, scratchBase = BigInt(0x90000000L)) ++
    new PGL22GSodorConfigBase
)

class PGL22GSodor3Config extends Config(
  new sodor.common.WithNSodorCores(1, internalTile = sodor.common.Stage3Factory(ports = 1), scratchBase = BigInt(0x90000000L)) ++
    new PGL22GSodorConfigBase
)

class PGL22GSodorUcodeConfig extends Config(
  new sodor.common.WithNSodorCores(1, internalTile = sodor.common.UCodeFactory, scratchBase = BigInt(0x90000000L)) ++
    new PGL22GSodorConfigBase
)