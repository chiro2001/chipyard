package chipyard.fpga.pgl22g

import sys.process._
import freechips.rocketchip.config.{Config, Parameters}
import freechips.rocketchip.subsystem.{ControlBusKey, ExtMem, PeripheryBusKey, SystemBusKey}
import freechips.rocketchip.devices.debug.{DebugModuleKey, ExportDebug, JTAG}
import freechips.rocketchip.devices.tilelink.{BootROMLocated, DevNullParams}
import freechips.rocketchip.diplomacy.{AddressSet, DTSModel, DTSTimebase, RegionType}
import freechips.rocketchip.tile.XLen
import sifive.blocks.devices.spi.{PeripherySPIKey, SPIParams}
import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTParams}
import sifive.fpgashells.shell.DesignKey
import sifive.fpgashells.shell.pango.pgl22gshell.PGL22GDDRSize
// import sifive.fpgashells.shell.pango.{PGL22GShellPMOD, PGL22GDDRSize}

import testchipip.{SerialTLKey}

import chipyard.{BuildSystem, ExtTLMem, DefaultClockFrequencyKey}

class WithDefaultPeripherals extends Config((site, here, up) => {
  case PeripheryUARTKey => List(UARTParams(address = BigInt(0x64000000L)))
  // case PeripherySPIKey => List(SPIParams(rAddress = BigInt(0x64001000L)))
  // case PGL22GShellPMOD => "SDIO"
})

class WithSystemModifications extends Config((site, here, up) => {
  case DTSTimebase => BigInt((1e6).toLong)
  case BootROMLocated(x) => up(BootROMLocated(x), site).map { p =>
    // invoke makefile for sdboot
    val freqMHz = (site(DefaultClockFrequencyKey) * 1e6).toLong
    val make = s"make -C fpga/src/main/resources/pgl22g/sdboot PBUS_CLK=${freqMHz} bin"
    require (make.! == 0, "Failed to build bootrom")
    p.copy(hang = 0x10000, contentFileName = s"./fpga/src/main/resources/pgl22g/sdboot/build/sdboot.bin")
  }
  case ExtMem => up(ExtMem, site).map(x => x.copy(master = x.master.copy(size = site(PGL22GDDRSize)))) // set extmem to DDR size
  case SerialTLKey => None // remove serialized tl port
})

// DOC include start: AbstractPGL22G and Rocket
class WithPGL22GTweaks extends Config(
  // harness binders
  new WithUART ++
  // new WithSPISDCard ++
  new WithDDRMem ++
  // io binders
  new WithUARTIOPassthrough ++
  // new WithSPIIOPassthrough ++
  // new WithTLIOPassthrough ++
  // other configuration
  new WithDefaultPeripherals ++
  new chipyard.config.WithTLBackingMemory ++ // use TL backing memory
  // new WithSystemModifications ++ // setup busses, use sdboot bootrom, setup ext. mem. size
  new chipyard.config.WithNoDebug ++ // remove debug module
  new freechips.rocketchip.subsystem.WithoutTLMonitors ++
  new freechips.rocketchip.subsystem.WithNMemoryChannels(1) ++
  new WithFPGAFrequency(100) // default 100MHz freq
)

class RocketPGL22GConfig extends Config(
  new WithPGL22GTweaks ++
  new chipyard.RocketConfig)
// DOC include end: AbstractPGL22G and Rocket

class BoomPGL22GConfig extends Config(
  new WithFPGAFrequency(50) ++
  new WithPGL22GTweaks ++
  new chipyard.MegaBoomConfig)

class WithFPGAFrequency(fMHz: Double) extends Config(
  new chipyard.config.WithPeripheryBusFrequency(fMHz) ++ // assumes using PBUS as default freq.
  new chipyard.config.WithMemoryBusFrequency(fMHz)
)

class WithFPGAFreq25MHz extends WithFPGAFrequency(25)
class WithFPGAFreq50MHz extends WithFPGAFrequency(50)
class WithFPGAFreq75MHz extends WithFPGAFrequency(75)
class WithFPGAFreq100MHz extends WithFPGAFrequency(100)
