package pgl22g

import chipsalliance.rocketchip.config.Config
import chipyard.iobinders.WithAXI4MemPunchthrough
import freechips.rocketchip.subsystem.{ExtMem, MasterPortParams, MemoryBusKey, MemoryPortParams, RocketTilesKey, WithBufferlessBroadcastHub, WithDefaultBtb, WithHypervisor, WithNBreakpoints, WithNTrackersPerBank}
import pgl22g._

class WithScratchpadsSize(startAddress: Long = 0x80000000L, sizeKB: Int = 16) extends Config((site, here, up) => {
  case RocketTilesKey => up(RocketTilesKey, site) map { r =>
    r.copy(
      core = r.core.copy(useVM = false),
      dcache = r.dcache.map(_.copy(
        nSets = sizeKB * 16,
        nWays = 1,
        scratch = Some(startAddress))))
  }
})

// class WithMemory extends Config((site, here, up) => {
//   // case ExtMem => None // disable AXI backing memory
//   case ExtTLMem => None
// })

class WithMemoryBusWidth(bitWidth: Int) extends Config((site, here, up) => {
  case MemoryBusKey => up(MemoryBusKey, site).copy(beatBytes = bitWidth / 8)
})

class WithPGL22GTLMem extends Config(
  new WithTLIOPassthrough ++
    new WithMemoryBusWidth(128) ++
    new chipyard.config.WithTLBackingMemory ++ // use TL backing memory
    new WithDDRMem ++
    // new WithBroadcastManager
    // Total 48 Kbit
    new freechips.rocketchip.subsystem.WithInclusiveCache(nWays = 2, capacityKB = 32, outerLatencyCycles = 3, subBankingFactor = 2)
)

class WithPGL22GAXIMem(base: BigInt = BigInt(0x80000000L)) extends Config(
  new WithMemoryBusWidth(128) ++
    new WithPGL22GMemPort(base = base) ++
    // new WithNBanks(0) ++ // Disable L2 Cache
    // new WithNBanks(1) ++
    new WithNBreakpoints(0) ++
    new WithHypervisor(false) ++
    new WithDefaultBtb ++
    new WithNTrackersPerBank(1) ++
    // new WithBroadcastManager ++
    new WithBufferlessBroadcastHub ++
    // new freechips.rocketchip.subsystem.WithInclusiveCache(nWays = 2, capacityKB = 16, outerLatencyCycles = 3, subBankingFactor = 2) ++
    new WithAXI4MemPunchthrough ++
    new WithBlackBoxDDRMem
)

class WithPGL22GAXIMemBare extends Config(
  new WithMemoryBusWidth(128) ++
    new WithPGL22GMemPort ++
    // new WithNBanks(0) ++
    new WithAXI4MemPunchthrough
)

class WithPGL22GMemPort(base: BigInt = BigInt(0x80000000L)) extends Config((site, here, up) => {
  case ExtMem => Some(MemoryPortParams(MasterPortParams(
    base = base,
    size = BigInt(0x10000000),
    beatBytes = site(MemoryBusKey).beatBytes,
    idBits = 4), 1))
})