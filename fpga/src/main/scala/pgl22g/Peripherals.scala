package pgl22g

import chipsalliance.rocketchip.config.Config
import freechips.rocketchip.diplomacy.DTSTimebase
import freechips.rocketchip.subsystem.ExtMem
import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTParams}
import sifive.fpgashells.shell.pango.PGL22GDDRSize
import testchipip.SerialTLKey
import pgl22g._

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