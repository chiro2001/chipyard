package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.Config

class PGL22GOnChipConfig extends Config(
  new chipyard.config.WithTLSerialLocation(
    freechips.rocketchip.subsystem.FBUS,
    freechips.rocketchip.subsystem.PBUS) ++                       // attach TL serial adapter to f/p busses
    new freechips.rocketchip.subsystem.WithIncoherentBusTopology ++ // use incoherent bus topology
    new freechips.rocketchip.subsystem.WithNBanks(0) ++             // remove L2$
    new freechips.rocketchip.subsystem.WithNoMemPort ++             // remove backing memory
    new freechips.rocketchip.subsystem.With1TinyCore ++             // single tiny rocket-core
    new chipyard.config.AbstractConfig)

class SimPGL22GOnChipConfig extends PGL22GOnChipConfig