package pgl22g.configs

import chipsalliance.rocketchip.config.Config
import freechips.rocketchip.diplomacy.AsynchronousCrossing
import freechips.rocketchip.subsystem._
import pgl22g._
import vexriscv.chipyard.{WithNVexRiscvCores, WithVexConfig}
import vexriscv.demo.VexOnChipConfig

import scala.language.postfixOps
import spinal.core._

class PGL22GVexRiscvBaseConfig extends Config(
  new WithUARTHarnessBinder ++
    // new WithDebugPeripherals ++
    // new WithJTAGHarnessBinder ++
    // new WithInternalJTAGIOCells ++
    // new WithInternalJTAGHarnessBinder ++
    // new WithInternalJTAGIOCells ++
    new WithNoDebug ++
    new WithSPIFlash ++
    new WithSPIFlashHarnessBinder ++
    // new WithTestsBootROM ++
    new WithVexRiscvBootROM ++
    new WithPGL22GTweaks ++
    new WithPGL22GAXIMem(width = 64) ++
    new WithNMemoryChannels(1) ++
    new WithBufferlessBroadcastHub ++
    new ModifiedAbstractConfig)

class PGL22GVexRiscvConfig extends Config(
  new WithNVexRiscvCores(1, onChipRAM = false) ++
    new WithVexConfig(VexOnChipConfig.default.copy(
      // iCacheSize = 16384,
      // dCacheSize = 16384,
      iCacheSize = 32 * 0x400,
      dCacheSize = 32 * 0x400,
      // iCacheSize = 4096,
      // dCacheSize = 4096,
      // iCacheSize = 0,
      // dCacheSize = 0,
      resetVector = 0x10000L,
      onChipRamSize = 0,
      freq = 50 MHz
    )) ++
    new WithFPGAFrequency(50.0) ++
    new PGL22GVexRiscvBaseConfig)

class PGL22GVexRiscvMultiClockConfig extends Config(
  new WithNVexRiscvCores(1, onChipRAM = false) ++
    new WithVexConfig(VexOnChipConfig.default.copy(
      // iCacheSize = 16384,
      // dCacheSize = 16384,
      iCacheSize = 32 * 0x400,
      dCacheSize = 32 * 0x400,
      // iCacheSize = 4096,
      // dCacheSize = 4096,
      // iCacheSize = 0,
      // dCacheSize = 0,
      resetVector = 0x10000L,
      onChipRamSize = 0
    )) ++
    // Frequency specifications
    new chipyard.config.WithTileFrequency(100.0) ++ // Matches the maximum frequency of U540
    new chipyard.config.WithSystemBusFrequency(50.0) ++ // Ditto
    new chipyard.config.WithMemoryBusFrequency(50.0) ++ // 2x the U540 freq (appropriate for a 128b Mbus)
    new chipyard.config.WithPeripheryBusFrequency(50) ++ // Retains the default pbus frequency
    new chipyard.config.WithSystemBusFrequencyAsDefault ++ // All unspecified clock frequencies, notably the implicit clock, will use the sbus freq (800 MHz)
    //  Crossing specifications
    new chipyard.config.WithCbusToPbusCrossingType(AsynchronousCrossing()) ++ // Add Async crossing between PBUS and CBUS
    new chipyard.config.WithSbusToMbusCrossingType(AsynchronousCrossing()) ++ // Add Async crossings between backside of L2 and MBUS
    new freechips.rocketchip.subsystem.WithRationalRocketTiles ++ // Add rational crossings between RocketTile and uncore
    new PGL22GVexRiscvBaseConfig)

class WithVexTLMem(width: Int = 64) extends Config(
  new WithTLIOPassthrough ++
    new WithMemoryBusWidth(width) ++
    new chipyard.config.WithTLBackingMemory ++ // use TL backing memory
    new WithDDRMem
    // new WithBroadcastManager
    // Total 48 Kbit
    // new freechips.rocketchip.subsystem.WithInclusiveCache(nWays = 2, capacityKB = 32, outerLatencyCycles = 3, subBankingFactor = 2)
)

class PGL22GVexRiscvTLBaseConfig extends Config(
  new WithUARTHarnessBinder ++
    // new WithDebugPeripherals ++
    // new WithJTAGHarnessBinder ++
    // new WithInternalJTAGIOCells ++
    // new WithInternalJTAGHarnessBinder ++
    // new WithInternalJTAGIOCells ++
    new WithNoDebug ++
    new WithSPIFlash ++
    new WithSPIFlashHarnessBinder ++
    // new WithTestsBootROM ++
    new WithVexRiscvBootROM ++
    // new WithFPGAFrequency(5.0) ++
    new WithPGL22GTweaks ++
    new WithVexTLMem(width = 64) ++
    new WithMemoryBusWidth(64) ++
    new WithNMemoryChannels(1) ++
    new WithBufferlessBroadcastHub ++
    new ModifiedAbstractConfig)

class PGL22GVexRiscvTLConfig extends Config(
  new WithNVexRiscvCores(1, onChipRAM = false) ++
    new WithVexConfig(VexOnChipConfig.default.copy(
      // iCacheSize = 16384,
      // dCacheSize = 16384,
      // iCacheSize = 32 * 0x400,
      // dCacheSize = 32 * 0x400,
      // iCacheSize = 4096,
      // dCacheSize = 4096,
      iCacheSize = 0,
      dCacheSize = 0,
      resetVector = 0x10000L,
      onChipRamSize = 0
    )) ++
    // Frequency specifications
    new chipyard.config.WithTileFrequency(100.0) ++ // Matches the maximum frequency of U540
    new chipyard.config.WithSystemBusFrequency(50.0) ++ // Ditto
    new chipyard.config.WithMemoryBusFrequency(50.0) ++ // 2x the U540 freq (appropriate for a 128b Mbus)
    new chipyard.config.WithPeripheryBusFrequency(50) ++ // Retains the default pbus frequency
    new chipyard.config.WithSystemBusFrequencyAsDefault ++ // All unspecified clock frequencies, notably the implicit clock, will use the sbus freq (800 MHz)
    //  Crossing specifications
    new chipyard.config.WithCbusToPbusCrossingType(AsynchronousCrossing()) ++ // Add Async crossing between PBUS and CBUS
    new chipyard.config.WithSbusToMbusCrossingType(AsynchronousCrossing()) ++ // Add Async crossings between backside of L2 and MBUS
    new freechips.rocketchip.subsystem.WithRationalRocketTiles ++ // Add rational crossings between RocketTile and uncore
    new PGL22GVexRiscvTLBaseConfig)

class PGL22GVexRiscvNConfig extends Config(
  new WithNVexRiscvCores(2, onChipRAM = false) ++
    new WithVexConfig(VexOnChipConfig.default.copy(
      iCacheSize = 4096,
      dCacheSize = 4096,
      resetVector = 0x10000L,
      onChipRamSize = 0,
      debug = false,
      freq = 25 MHz
    )) ++
    new WithFPGAFrequency(25.0) ++
    new PGL22GVexRiscvBaseConfig)

class SimPGL22GVexRiscvConfig extends Config(
  new WithNVexRiscvCores(1, onChipRAM = false) ++
    new WithVexConfig(VexOnChipConfig.default.copy(
      // iCacheSize = 16384,
      // dCacheSize = 16384,
      iCacheSize = 8192,
      dCacheSize = 8192,
      // iCacheSize = 4096,
      // dCacheSize = 4096,
      // iCacheSize = 0,
      // dCacheSize = 0,
      resetVector = 0x10040L,
      onChipRamSize = 0
    )) ++
    // new WithMemoryBusWidth(32) ++
    new WithMemoryBusWidth(64) ++
    new WithPGL22GSimTinyTweaks ++
    new WithFPGAFrequency(5.0) ++
    new ModifiedAbstractConfig
)

class SimPGL22GVexRiscvNConfig extends Config(
  new WithNVexRiscvCores(2, onChipRAM = false) ++
    new WithVexConfig(VexOnChipConfig.default.copy(
      iCacheSize = 4096,
      dCacheSize = 4096,
      resetVector = 0x10040L,
      onChipRamSize = 0
    )) ++
    new WithMemoryBusWidth(64) ++
    new WithPGL22GSimTinyTweaks ++
    new WithFPGAFrequency(5.0) ++
    new ModifiedAbstractConfig
)

class SimPGL22GVexRiscvSpiConfig extends Config(
  new WithNVexRiscvCores(1, onChipRAM = false) ++
    new WithVexConfig(VexOnChipConfig.default.copy(
      // iCacheSize = 16384,
      // dCacheSize = 16384,
      iCacheSize = 4096,
      dCacheSize = 4096,
      // iCacheSize = 0,
      // dCacheSize = 0,
      resetVector = 0x10080L,
      onChipRamSize = 0
    )) ++
    new WithMemoryBusWidth(32) ++
    new WithPGL22GSimTinyTweaks ++
    new WithFPGAFrequency(5.0) ++
    new WithSimSPIFlash ++
    new WithVexRiscvBootROM(true) ++
    new ModifiedAbstractConfig
)

class SimPGL22GVexRiscvNSpiConfig extends Config(
  new WithNVexRiscvCores(2, onChipRAM = false) ++
    new WithVexConfig(VexOnChipConfig.default.copy(
      iCacheSize = 4096,
      dCacheSize = 4096,
      resetVector = 0x10080L,
      onChipRamSize = 0
    )) ++
    new WithMemoryBusWidth(32) ++
    new WithPGL22GSimTinyTweaks ++
    new WithFPGAFrequency(5.0) ++
    new WithSimSPIFlash ++
    new WithVexRiscvBootROM(true) ++
    new ModifiedAbstractConfig
)