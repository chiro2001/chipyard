package chipyard.fpga.pgl22g.bringup

import chisel3._

import freechips.rocketchip.diplomacy._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._

import sifive.fpgashells.shell.xilinx._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.shell._
import sifive.fpgashells.clocks._

import sifive.blocks.devices.uart._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.i2c._
import sifive.blocks.devices.gpio._

import testchipip.{HasPeripheryTSIHostWidget, PeripheryTSIHostKey, TSIHostWidgetIO, TLSinkSetter}

import chipyard.fpga.pgl22g.{PGL22GFPGATestHarness, PGL22GFPGATestHarnessImp, DDR2PGL22GShellPlacer, SysClock2PGL22GShellPlacer}

import chipyard.{ChipTop}

class BringupPGL22GFPGATestHarness(override implicit val p: Parameters) extends PGL22GFPGATestHarness {

  /*** UART ***/

  require(dp(PeripheryUARTKey).size == 2)

  // 2nd UART goes to the FMC UART

  val uart_fmc = Overlay(UARTOverlayKey, new BringupUARTPGL22GShellPlacer(this, UARTShellInput()))

  val io_fmc_uart_bb = BundleBridgeSource(() => (new UARTPortIO(dp(PeripheryUARTKey).last)))
  dp(UARTOverlayKey).last.place(UARTDesignInput(io_fmc_uart_bb))
  //
  // /*** I2C ***/
  //
  // val i2c = Overlay(I2COverlayKey, new BringupI2CPGL22GShellPlacer(this, I2CShellInput()))
  //
  // val io_i2c_bb = BundleBridgeSource(() => (new I2CPort))
  // dp(I2COverlayKey).head.place(I2CDesignInput(io_i2c_bb))
  //
  // /*** GPIO ***/
  //
  // val gpio = Seq.tabulate(dp(PeripheryGPIOKey).size)(i => {
  //   val maxGPIOSupport = 32 // max gpio per gpio chip
  //   val names = BringupGPIOs.names.slice(maxGPIOSupport*i, maxGPIOSupport*(i+1))
  //   Overlay(GPIOOverlayKey, new BringupGPIOPGL22GShellPlacer(this, GPIOShellInput(), names))
  // })
  //
  // val io_gpio_bb = dp(PeripheryGPIOKey).map { p => BundleBridgeSource(() => (new GPIOPortIO(p))) }
  // (dp(GPIOOverlayKey) zip dp(PeripheryGPIOKey)).zipWithIndex.map { case ((placer, params), i) =>
  //   placer.place(GPIODesignInput(params, io_gpio_bb(i)))
  // }

  /*** TSI Host Widget ***/
  require(dp(PeripheryTSIHostKey).size == 1)

  // use the 2nd system clock for the 2nd DDR
  val sysClk2Node = dp(ClockInputOverlayKey).last.place(ClockInputDesignInput()).overlayOutput.node

  val ddr2PLL = dp(PLLFactoryKey)()
  ddr2PLL := sysClk2Node

  val ddr2Clock = ClockSinkNode(freqMHz = dp(FPGAFrequencyKey))
  val ddr2Wrangler = LazyModule(new ResetWrangler)
  val ddr2Group = ClockGroup()
  ddr2Clock := ddr2Wrangler.node := ddr2Group := ddr2PLL

  val tsi_host = Overlay(TSIHostOverlayKey, new BringupTSIHostPGL22GShellPlacer(this, TSIHostShellInput()))

  val ddr2Node = dp(DDROverlayKey).last.place(DDRDesignInput(dp(PeripheryTSIHostKey).head.targetMasterPortParams.base, ddr2Wrangler.node, ddr2PLL)).overlayOutput.ddr

  val io_tsi_serial_bb = BundleBridgeSource(() => (new TSIHostWidgetIO(dp(PeripheryTSIHostKey).head.offchipSerialIfWidth)))
  dp(TSIHostOverlayKey).head.place(TSIHostDesignInput(dp(PeripheryTSIHostKey).head.offchipSerialIfWidth, io_tsi_serial_bb))

  // connect 1 mem. channel to the FPGA DDR
  val inTsiParams = topDesign match { case td: ChipTop =>
    td.lazySystem match { case lsys: HasPeripheryTSIHostWidget =>
      lsys.tsiMemTLNodes.head.edges.in(0)
    }
  }
  val tsiDdrClient = TLClientNode(Seq(inTsiParams.master))
  (ddr2Node
    := TLFragmenter(8,64,holdFirstDeny=true)
    := TLCacheCork()
    := TLAtomicAutomata(passthrough=false)
    := TLSinkSetter(64)
    := tsiDdrClient)

  // module implementation
  override lazy val module = new BringupPGL22GFPGATestHarnessImp(this)
}

class BringupPGL22GFPGATestHarnessImp(_outer: BringupPGL22GFPGATestHarness) extends PGL22GFPGATestHarnessImp(_outer) {
  lazy val bringupOuter = _outer
}
