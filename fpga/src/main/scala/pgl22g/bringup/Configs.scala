package chipyard.fpga.pgl22g.bringup

import math.min

import freechips.rocketchip.config.{Config, Parameters}
import freechips.rocketchip.diplomacy.{DTSModel, DTSTimebase, RegionType, AddressSet, ResourceBinding, Resource, ResourceAddress}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.{MasterPortParams}

import sifive.blocks.devices.gpio.{PeripheryGPIOKey, GPIOParams}
import sifive.blocks.devices.i2c.{PeripheryI2CKey, I2CParams}
import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTParams}

import sifive.fpgashells.shell.{DesignKey}

import testchipip.{PeripheryTSIHostKey, TSIHostParams, TSIHostSerdesParams}

import chipyard.{BuildSystem}

import chipyard.fpga.pgl22g.{WithPGL22GTweaks, WithFPGAFrequency, PGL22GDDR2Size}
import sifive.fpgashells.shell.pango.pgl22gshell.{PGL22GDDRSize, PGL22GShellPMOD}

class WithBringupPeripherals extends Config((site, here, up) => {
  case PeripheryUARTKey => up(PeripheryUARTKey, site) ++ List(UARTParams(address = BigInt(0x64003000L)))
  case PeripheryI2CKey => List(I2CParams(address = BigInt(0x64005000L)))
  case PeripheryGPIOKey => {
    if (BringupGPIOs.width > 0) {
      require(BringupGPIOs.width <= 64) // currently only support 64 GPIOs (change addrs to get more)
      val gpioAddrs = Seq(BigInt(0x64002000), BigInt(0x64007000))
      val maxGPIOSupport = 32 // max gpios supported by SiFive driver (split by 32)
      List.tabulate(((BringupGPIOs.width - 1)/maxGPIOSupport) + 1)(n => {
        GPIOParams(address = gpioAddrs(n), width = min(BringupGPIOs.width - maxGPIOSupport*n, maxGPIOSupport))
      })
    }
    else {
      List.empty[GPIOParams]
    }
  }
  case TSIClockMaxFrequencyKey => 100
  case PeripheryTSIHostKey => List(
    TSIHostParams(
      offchipSerialIfWidth = 4,
      mmioBaseAddress = BigInt(0x64006000),
      mmioSourceId = 1 << 13, // manager source
      serdesParams = TSIHostSerdesParams(
        clientPortParams = TLMasterPortParameters.v1(
          clients = Seq(TLMasterParameters.v1(
            name = "tl-tsi-host-serdes",
            sourceId = IdRange(0, (1 << 13))))),
        managerPortParams = TLSlavePortParameters.v1(
          managers = Seq(TLSlaveParameters.v1(
            address = Seq(AddressSet(0, BigInt("FFFFFFFF", 16))), // access everything on chip
            regionType = RegionType.UNCACHED,
            executable = true,
            supportsGet        = TransferSizes(1, 64),
            supportsPutFull    = TransferSizes(1, 64),
            supportsPutPartial = TransferSizes(1, 64),
            supportsAcquireT   = TransferSizes(1, 64),
            supportsAcquireB   = TransferSizes(1, 64),
            supportsArithmetic = TransferSizes(1, 64),
            supportsLogical    = TransferSizes(1, 64))),
          endSinkId = 1 << 6, // manager sink
          beatBytes = 8)),
      targetMasterPortParams = MasterPortParams(
        base = BigInt("80000000", 16),
        size = site(PGL22GDDR2Size),
        beatBytes = 8, // comes from test chip
        idBits = 4) // comes from PGL22G idBits in XilinxPGL22GMIG
      ))
})

class WithBringupPGL22GSystem extends Config((site, here, up) => {
  case BuildSystem => (p: Parameters) => new BringupPGL22GDigitalTop()(p) // use the PGL22G-extended bringup digital top
})

class WithBringupAdditions extends Config(
  new WithBringupUART ++
  // new WithBringupI2C ++
  // new WithBringupGPIO ++
  new WithBringupTSIHost ++
  new WithTSITLIOPassthrough ++
  new WithI2CIOPassthrough ++
  new WithGPIOIOPassthrough ++
  new WithBringupPeripherals ++
  new WithBringupPGL22GSystem)

class RocketBringupConfig extends Config(
  new WithBringupAdditions ++
  new WithPGL22GTweaks ++
  new chipyard.RocketConfig)

class BoomBringupConfig extends Config(
  new WithFPGAFrequency(50) ++
  new WithBringupAdditions ++
  new WithPGL22GTweaks ++
  new chipyard.MegaBoomConfig)
