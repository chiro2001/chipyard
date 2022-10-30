package pgl22g.testharness

import chipyard._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import shell.pango.{DDRDesignInputSysClk, DDROverlayKeySysClk}
import sifive.blocks.devices.uart._
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.shell.pango.PGL22GShellDDROverlays

class PGL22GTestHarness(override implicit val p: Parameters) extends PGL22GShellDDROverlays {

  def dp = designParameters

  // val pmod_is_sdio  = p(PGL22GShellPMOD) == "SDIO"
  // val jtag_location = Some(if (pmod_is_sdio) "FMC_J2" else "PMOD_J52")

  // Order matters; ddr depends on sys_clock
  // val uart      = Overlay(UARTOverlayKey, new UARTPGL22GShellPlacer(this, UARTShellInput()))
  // val sdio      = if (pmod_is_sdio) Some(Overlay(SPIOverlayKey, new SDIOPGL22GShellPlacer(this, SPIShellInput()))) else None
  // val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugPGL22GShellPlacer(this, JTAGDebugShellInput(location = jtag_location)))
  // val cjtag     = Overlay(cJTAGDebugOverlayKey, new cJTAGDebugPGL22GShellPlacer(this, cJTAGDebugShellInput()))
  // val jtagBScan = Overlay(JTAGDebugBScanOverlayKey, new JTAGDebugBScanPGL22GShellPlacer(this, JTAGDebugBScanShellInput()))
  // val fmc       = Overlay(PCIeOverlayKey, new PCIePGL22GFMCShellPlacer(this, PCIeShellInput()))
  // val edge      = Overlay(PCIeOverlayKey, new PCIePGL22GEdgeShellPlacer(this, PCIeShellInput()))
  // val sys_clock2 = Overlay(ClockInputOverlayKey, new SysClock2PGL22GShellPlacer(this, ClockInputShellInput()))
  // val ddr2       = Overlay(DDROverlayKey, new DDR2PGL22GShellPlacer(this, DDRShellInput()))

  val topDesign = LazyModule(p(BuildTop)(dp)).suggestName("chiptop")

  // DOC include start: ClockOverlay
  // place all clocks in the shell
  require(dp(ClockInputOverlayKey).size >= 1)
  val sysClkNode = dp(ClockInputOverlayKey)(0).place(ClockInputDesignInput()).overlayOutput.node

  val dutWrangler = LazyModule(new ResetWrangler)

  /** * DDR ** */

  val mig = dp(DDROverlayKeySysClk).head.place(DDRDesignInputSysClk(dp(ExtTLMem).get.master.base, dutWrangler.node, sysClkNode)).overlayOutput.mig
  val ddrNode = mig.node

  // connect 1 mem. channel to the FPGA DDR
  val inParams = topDesign match {
    case td: ChipTop =>
      td.lazySystem match {
        case lsys: CanHaveMasterTLMemPort =>
          lsys.memTLNode.edges.in(0)
      }
  }
  val ddrClient = TLClientNode(Seq(inParams.master))
  ddrNode := ddrClient

  val migUIClock = mig.pllNode
  migUIClock := sysClkNode
  // sysClkNode := migUIClock

  /** * Connect/Generate clocks ** */

  // connect to the PLL that will generate multiple clocks
  // val harnessSysPLL = dp(PLLFactoryKey)()
  // harnessSysPLL := sysClkNode

  // create and connect to the dutClock
  println(s"PGL22G FPGA Base Clock Freq: ${dp(DefaultClockFrequencyKey)} MHz")
  val dutClock = ClockSinkNode(freqMHz = dp(DefaultClockFrequencyKey))
  val dutGroup = ClockGroup()
  // dutClock := dutWrangler.node := dutGroup := harnessSysPLL
  dutClock := dutWrangler.node := dutGroup := migUIClock
  // DOC include end: ClockOverlay

  /** * UART ** */

  // DOC include start: UartOverlay
  // 1st UART goes to the PGL22G dedicated UART

  val io_uart_bb = BundleBridgeSource(() => (new UARTPortIO(dp(PeripheryUARTKey).head)))
  dp(UARTOverlayKey).head.place(UARTDesignInput(io_uart_bb))
  // DOC include end: UartOverlay

  // /*** SPI ***/
  //
  // // 1st SPI goes to the PGL22G SDIO port
  //
  // val io_spi_bb = BundleBridgeSource(() => (new SPIPortIO(dp(PeripherySPIKey).head)))
  // dp(SPIOverlayKey).head.place(SPIDesignInput(dp(PeripherySPIKey).head, io_spi_bb))

  // module implementation
  override lazy val module = new PGL22GTestHarnessImp(this)
}
