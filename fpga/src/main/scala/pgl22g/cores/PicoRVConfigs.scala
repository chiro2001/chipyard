package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.Config
import chipyard.config.WithL2TLBs
import freechips.rocketchip.subsystem.{WithBufferlessBroadcastHub, WithL1DCacheSets, WithL1ICacheSets, WithNMemoryChannels, WithoutFPU}

class PGL22GPicoRVConfig extends Config(
  new picorv.WithNPicoRVCores(1) ++
    new WithPGL22GTweaks ++
    new WithPGL22GAXIMem ++
    new WithoutFPU ++
    new WithL2TLBs(0) ++
    new WithL1ICacheSets(64 * 2) ++
    new WithL1DCacheSets(64 * 2) ++
    new WithNMemoryChannels(1) ++
    new WithBufferlessBroadcastHub ++
    new WithoutBootROM ++
    new ModifiedAbstractConfig)

class SimPGL22GPicoRVConfig extends Config(
  new picorv.WithNPicoRVCores(1) ++
    new WithMemoryBusWidth(32) ++
    new WithPGL22GSimTinyTweaks ++
    new WithPicoRVBootROM ++
    new ModifiedAbstractConfig
)
