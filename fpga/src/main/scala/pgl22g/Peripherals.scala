package pgl22g

import chipsalliance.rocketchip.config.Config
import freechips.rocketchip.devices.debug.{JtagDTMConfig, JtagDTMKey}
import freechips.rocketchip.diplomacy.DTSTimebase
import freechips.rocketchip.subsystem.ExtMem
import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTParams}
import sifive.fpgashells.shell.pango.PGL22GDDRSize
import testchipip.SerialTLKey
import pgl22g._
import sifive.blocks.devices.spi.{PeripherySPIFlashKey, SPIFlashParams}

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
  case PeripheryUARTKey => List(UARTParams(address = BigInt(0x54000000L)))
  // case PeripherySPIKey => List(SPIParams(rAddress = BigInt(0x64001000L)))
  // case VCU118ShellPMOD => "SDIO"
})

class WithDDRPeripherals extends Config((site, here, up) => {
  case ExtMem => up(ExtMem, site).map(x => x.copy(master = x.master.copy(size = site(PGL22GDDRSize)))) // set extmem to DDR size
  case SerialTLKey => None // remove serialized tl port
})

class WithFPGAFrequency(fMHz: Double) extends Config(
  new chipyard.config.WithPeripheryBusFrequency(fMHz) ++ // assumes using PBUS as default freq.
    new chipyard.config.WithMemoryBusFrequency(fMHz)
)

class WithDebugPeripherals extends Config((site, here, up) => {
  case JtagDTMKey => new JtagDTMConfig(
    idcodeVersion = 2,
    idcodePartNum = 0x000,
    idcodeManufId = 0x489,
    debugIdleCycles = 5)
})

// default 128M bit = 16MB
class WithSPIFlash(size: BigInt = 16 * 1024 * 1024) extends Config((site, here, up) => {
  // Note: the default size matches freedom with the addresses below
  case PeripherySPIFlashKey => Seq(
    SPIFlashParams(rAddress = 0x10040000, fAddress = 0x20000000, fSize = size))
})

class WithSimSPIFlash(size: BigInt = 16 * 1024 * 1024) extends Config((site, here, up) => {
  case PeripherySPIFlashKey => Seq(
    SPIFlashParams(rAddress = 0x10040000, fAddress = 0x20000000, fSize = size, divisorBits = 2))
})