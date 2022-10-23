// See LICENSE for license details.
package chipyard.fpga.pgl22g

import chipyard.{ExtTLMem, TinyRocketConfig}
import chipyard.config.{AbstractConfig, WithBroadcastManager, WithL2TLBs, WithSystemBusWidth}
import chipyard.harness.WithBlackBoxSimMem
import chipyard.iobinders.WithAXI4MemPunchthrough
import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.diplomacy.{DTSTimebase, SynchronousCrossing}
import freechips.rocketchip.rocket.{DCacheParams, ICacheParams, MulDivParams, RocketCoreParams}
import freechips.rocketchip.subsystem._
import freechips.rocketchip.system.{BaseConfig, MemPortOnlyConfig, TinyConfig}
import freechips.rocketchip.tile.{RocketTileParams, XLen}
import sifive.blocks.devices.uart._
import sifive.fpgashells.shell.pango.PGL22GDDRSize
import testchipip.{SerialTLKey, WithRingSystemBus, WithSerialTLMem}

class ModifiedAbstractConfig extends Config(
  // The HarnessBinders control generation of hardware in the TestHarness
  new chipyard.harness.WithUARTAdapter ++ // add UART adapter to display UART on stdout, if uart is present
    // new chipyard.harness.WithBlackBoxSimMem ++                    // add SimDRAM DRAM model for axi4 backing memory, if axi4 mem is enabled
    // new chipyard.harness.WithSimSerial ++ // add external serial-adapter and RAM
    // new chipyard.harness.WithSimDebug ++ // add SimJTAG or SimDTM adapters if debug module is enabled
    new chipyard.harness.WithGPIOTiedOff ++ // tie-off chiptop GPIOs, if GPIOs are present
    // new chipyard.harness.WithSimSPIFlashModel ++ // add simulated SPI flash memory, if SPI is enabled
    // new chipyard.harness.WithSimAXIMMIO ++ // add SimAXIMem for axi4 mmio port, if enabled
    new chipyard.harness.WithTieOffInterrupts ++ // tie-off interrupt ports, if present
    new chipyard.harness.WithTieOffL2FBusAXI ++ // tie-off external AXI4 master, if present
    new chipyard.harness.WithCustomBootPinPlusArg ++
    new chipyard.harness.WithClockAndResetFromHarness ++

    // The IOBinders instantiate ChipTop IOs to match desired digital IOs
    // IOCells are generated for "Chip-like" IOs, while simulation-only IOs are directly punched through
    new chipyard.iobinders.WithAXI4MemPunchthrough ++
    new chipyard.iobinders.WithAXI4MMIOPunchthrough ++
    new chipyard.iobinders.WithL2FBusAXI4Punchthrough ++
    new chipyard.iobinders.WithBlockDeviceIOPunchthrough ++
    new chipyard.iobinders.WithNICIOPunchthrough ++
    new chipyard.iobinders.WithSerialTLIOCells ++
    new chipyard.iobinders.WithDebugIOCells ++
    new chipyard.iobinders.WithUARTIOCells ++
    new chipyard.iobinders.WithGPIOCells ++
    new chipyard.iobinders.WithSPIIOCells ++
    new chipyard.iobinders.WithTraceIOPunchthrough ++
    new chipyard.iobinders.WithExtInterruptIOCells ++
    new chipyard.iobinders.WithCustomBootPin ++
    new chipyard.iobinders.WithDividerOnlyClockGenerator ++

    new testchipip.WithSerialTLWidth(32) ++ // fatten the serialTL interface to improve testing performance
    new testchipip.WithDefaultSerialTL ++ // use serialized tilelink port to external serialadapter/harnessRAM
    new chipyard.config.WithBootROM ++ // use default bootrom
    new chipyard.config.WithUART ++ // add a UART
    new chipyard.config.WithL2TLBs(1024) ++ // use L2 TLBs
    new chipyard.config.WithNoSubsystemDrivenClocks ++ // drive the subsystem diplomatic clocks from ChipTop instead of using implicit clocks
    new chipyard.config.WithInheritBusFrequencyAssignments ++ // Unspecified clocks within a bus will receive the bus frequency if set
    new chipyard.config.WithPeripheryBusFrequencyAsDefault ++ // Unspecified frequencies with match the pbus frequency (which is always set)
    new chipyard.config.WithMemoryBusFrequency(100.0) ++ // Default 100 MHz mbus
    new chipyard.config.WithPeripheryBusFrequency(100.0) ++ // Default 100 MHz pbus
    new freechips.rocketchip.subsystem.WithJtagDTM ++ // set the debug module to expose a JTAG port
    new freechips.rocketchip.subsystem.WithNoMMIOPort ++ // no top-level MMIO master port (overrides default set in rocketchip)
    new freechips.rocketchip.subsystem.WithNoSlavePort ++ // no top-level MMIO slave port (overrides default set in rocketchip)
    // new freechips.rocketchip.subsystem.WithInclusiveCache ++ // use Sifive L2 cache
    new freechips.rocketchip.subsystem.WithNExtTopInterrupts(0) ++ // no external interrupts
    new freechips.rocketchip.subsystem.WithDontDriveBusClocksFromSBus ++ // leave the bus clocks undriven by sbus
    new freechips.rocketchip.subsystem.WithCoherentBusTopology ++ // hierarchical buses including sbus/mbus/pbus/fbus/cbus/l2
    new freechips.rocketchip.system.BaseConfig) // "base" rocketchip system

class WithDefaultTimebase extends Config((site, here, up) => {
  case DTSTimebase => BigInt(1e6.toLong)
})

class WithDefaultPeripherals extends Config((site, here, up) => {
  // case PeripheryUARTKey => List(
  //   UARTParams(address = 0x10013000))
  // case DTSTimebase => BigInt(32768)
  // case JtagDTMKey => new JtagDTMConfig(
  //   idcodeVersion = 2,
  //   idcodePartNum = 0x000,
  //   idcodeManufId = 0x489,
  //   debugIdleCycles = 5)
  case SerialTLKey => None // remove serialized tl port
  case PeripheryUARTKey => List(UARTParams(address = BigInt(0x64000000L)))
  // case PeripherySPIKey => List(SPIParams(rAddress = BigInt(0x64001000L)))
  // case VCU118ShellPMOD => "SDIO"
})

class WithSystemModifications extends Config((site, here, up) => {
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
    new ModifiedAbstractConfig)

class WithNoDebug extends Config((site, here, up) => {
  case DebugModuleKey => None
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

class BasePerfConfig extends Config(
  // new chipyard.config.WithTLSerialLocation(
  //   freechips.rocketchip.subsystem.FBUS,
  //   freechips.rocketchip.subsystem.PBUS) ++ // attach TL serial adapter to f/p busses
  new freechips.rocketchip.subsystem.WithCoherentBusTopology ++ // use incoherent bus topology
    // new freechips.rocketchip.subsystem.WithIncoherentBusTopology ++ // use incoherent bus topology
    // new freechips.rocketchip.subsystem.WithNBanks(0) ++ // remove L2$
    // new freechips.rocketchip.subsystem.WithNoMemPort ++             // remove backing memory
    new WithPGL22GMemPort ++
    new WithSystemModifications ++
    new freechips.rocketchip.subsystem.With1TinyCore ++ // single tiny rocket-core
    new ModifiedAbstractConfig)

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
    new WithPGL22GTinyCore ++
    new PGL22GRocketConfig
)

class WithSmallScratchpadsOnly extends Config((site, here, up) => {
  case RocketTilesKey => up(RocketTilesKey, site) map { r =>
    r.copy(
      core = r.core.copy(useVM = false),
      dcache = r.dcache.map(_.copy(
        nSets = 256 / 16, // 16/16Kb scratchpad
        nWays = 1,
        scratch = Some(0x80000000L))))
  }
})

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
    new ModifiedAbstractConfig)

class PGL22GSSRVConfig extends Config(
  new ssrv.WithNSSRVCores(1) ++
    new WithPGL22GTweaks ++
    new WithPGL22GAXIMem ++
    new WithoutFPU ++
    new WithL2TLBs(0) ++
    new WithL1ICacheSets(64 * 2) ++
    new WithL1DCacheSets(64 * 2) ++
    new WithNMemoryChannels(1) ++
    new WithBufferlessBroadcastHub ++
    new ModifiedAbstractConfig)

class SimTinyRocketPGL22GConfig extends Config(
  new WithPGL22GSimTweaks ++
    new PGL22GRocketConfig
)