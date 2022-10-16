// See LICENSE for license details.
package chipyard.fpga.pgl22g

import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy.{DTSModel, DTSTimebase}
import freechips.rocketchip.system._
import freechips.rocketchip.tile._
import sifive.blocks.devices.uart._
import testchipip.SerialTLKey
import chipyard.BuildSystem
import chipyard.config.WithL2TLBs

class WithDefaultPeripherals extends Config((site, here, up) => {
  case PeripheryUARTKey => List(
    UARTParams(address = 0x10013000))
  case DTSTimebase => BigInt(32768)
  case JtagDTMKey => new JtagDTMConfig(
    idcodeVersion = 2,
    idcodePartNum = 0x000,
    idcodeManufId = 0x489,
    debugIdleCycles = 5)
  case SerialTLKey => None // remove serialized tl port
})


class AbstractConfig extends Config(
  // The HarnessBinders control generation of hardware in the TestHarness
  new chipyard.harness.WithUARTAdapter ++                       // add UART adapter to display UART on stdout, if uart is present
    new chipyard.harness.WithBlackBoxSimMem ++                    // add SimDRAM DRAM model for axi4 backing memory, if axi4 mem is enabled
    new chipyard.harness.WithSimSerial ++                         // add external serial-adapter and RAM
    // new chipyard.harness.WithSimDebug ++                          // add SimJTAG or SimDTM adapters if debug module is enabled
    new chipyard.harness.WithGPIOTiedOff ++                       // tie-off chiptop GPIOs, if GPIOs are present
    new chipyard.harness.WithSimSPIFlashModel ++                  // add simulated SPI flash memory, if SPI is enabled
    new chipyard.harness.WithSimAXIMMIO ++                        // add SimAXIMem for axi4 mmio port, if enabled
    new chipyard.harness.WithTieOffInterrupts ++                  // tie-off interrupt ports, if present
    new chipyard.harness.WithTieOffL2FBusAXI ++                   // tie-off external AXI4 master, if present
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
    // new chipyard.iobinders.WithCustomBootPin ++
    new chipyard.iobinders.WithDividerOnlyClockGenerator ++

    new testchipip.WithSerialTLWidth(32) ++                           // fatten the serialTL interface to improve testing performance
    new testchipip.WithDefaultSerialTL ++                             // use serialized tilelink port to external serialadapter/harnessRAM
    new chipyard.config.WithBootROM ++                                // use default bootrom
    new chipyard.config.WithUART ++                                   // add a UART
    // new chipyard.config.WithL2TLBs(1024) ++                           // use L2 TLBs
    new chipyard.config.WithNoSubsystemDrivenClocks ++                // drive the subsystem diplomatic clocks from ChipTop instead of using implicit clocks
    new chipyard.config.WithInheritBusFrequencyAssignments ++         // Unspecified clocks within a bus will receive the bus frequency if set
    new chipyard.config.WithPeripheryBusFrequencyAsDefault ++         // Unspecified frequencies with match the pbus frequency (which is always set)
    new chipyard.config.WithMemoryBusFrequency(50.0) ++              // Default 100 MHz mbus
    new chipyard.config.WithPeripheryBusFrequency(50.0) ++           // Default 100 MHz pbus
    new freechips.rocketchip.subsystem.WithJtagDTM ++                 // set the debug module to expose a JTAG port
    new freechips.rocketchip.subsystem.WithNoMMIOPort ++              // no top-level MMIO master port (overrides default set in rocketchip)
    new freechips.rocketchip.subsystem.WithNoSlavePort ++             // no top-level MMIO slave port (overrides default set in rocketchip)
    new freechips.rocketchip.subsystem.WithInclusiveCache ++          // use Sifive L2 cache
    new freechips.rocketchip.subsystem.WithNExtTopInterrupts(0) ++    // no external interrupts
    new freechips.rocketchip.subsystem.WithDontDriveBusClocksFromSBus ++ // leave the bus clocks undriven by sbus
    new freechips.rocketchip.subsystem.WithCoherentBusTopology ++     // hierarchical buses including sbus/mbus/pbus/fbus/cbus/l2
    new freechips.rocketchip.system.BaseConfig)                       // "base" rocketchip system


class PGL22GRocketConfig extends Config(
  new chipyard.config.WithTLSerialLocation(FBUS, PBUS) ++ // attach TL serial adapter to f/p busses
    new WithIncoherentBusTopology ++ // use incoherent bus topology
    new WithNBanks(0) ++ // remove L2$
    new WithL1ICacheWays(1) ++
    new WithL1ICacheSets(64) ++
    new WithL1DCacheWays(1) ++
    new WithL1DCacheSets(64) ++
    new WithoutFPU ++
    new WithoutMulDiv ++
    new WithNoMemPort ++ // remove backing memory
    new With1TinyCore ++ // single tiny rocket-core
    new WithRV32 ++ // set RocketTiles to be 32-bit
    new AbstractConfig)

class WithNoDebug extends Config((site, here, up) => {
  case DebugModuleKey => None
})

// DOC include start: AbstractPGL22G and Rocket
class WithPGL22GTweaks extends Config(
  // new WithPGL22GJTAGHarnessBinder ++
  // new WithTimebase(32000000) ++ // 32Mhz CPU Clock default
  new WithTimebase(50000000) ++ // 50Mhz CPU Clock default
    new WithPGL22GUARTHarnessBinder ++
    // new WithPGL22GResetHarnessBinder ++
    // new WithDebugResetPassthrough ++
    new WithDefaultPeripherals ++
    new WithNoDebug ++
    new freechips.rocketchip.subsystem.WithoutTLMonitors ++
    new freechips.rocketchip.subsystem.WithNBreakpoints(2))

class TinyRocketPGL22GConfig extends Config(
  new WithPGL22GTweaks ++
    new PGL22GRocketConfig)
// DOC include end: AbstractPGL22G and Rocket
