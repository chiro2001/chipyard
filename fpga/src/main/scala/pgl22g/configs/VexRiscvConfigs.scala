package pgl22g.configs

import chipsalliance.rocketchip.config.Config
import chipyard.config.WithL2TLBs
import freechips.rocketchip.subsystem.{WithBufferlessBroadcastHub, WithL1DCacheSets, WithL1ICacheSets, WithNMemoryChannels, WithoutFPU}
import pgl22g._
import vexriscv.chipyard.WithNVexRiscvCores

class PGL22GVexRiscvConfig extends Config(
  new WithNVexRiscvCores(1) ++
    new WithPGL22GTweaks ++
    new WithPGL22GAXIMem ++
    new WithoutFPU ++
    new WithL2TLBs(0) ++
    new WithL1ICacheSets(64 * 2) ++
    new WithL1DCacheSets(64 * 2) ++
    new WithNMemoryChannels(1) ++
    new WithBufferlessBroadcastHub ++
    new ModifiedAbstractConfig)

class PGL22GVexRiscv2Config extends Config(
  new WithNVexRiscvCores(2) ++
    new WithPGL22GTweaks ++
    new WithPGL22GAXIMem ++
    new WithoutFPU ++
    new WithL2TLBs(0) ++
    new WithL1ICacheSets(64 * 2) ++
    new WithL1DCacheSets(64 * 2) ++
    new WithNMemoryChannels(1) ++
    new WithBufferlessBroadcastHub ++
    new ModifiedAbstractConfig)

class SimPGL22GVexRiscvConfig extends Config(
  new WithNVexRiscvCores(1) ++
    new WithMemoryBusWidth(32) ++
    new WithPGL22GSimTinyTweaks ++
    new WithVexRiscvBootROM ++
    new ModifiedAbstractConfig
)