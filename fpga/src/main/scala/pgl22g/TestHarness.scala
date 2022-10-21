package chipyard.fpga.pgl22g

import chipyard._
import chipyard.harness.ApplyHarnessBinders
import chipyard.iobinders.HasIOBinders
import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import shell.pango.{DDRDesignInputSysClk, DDROverlayKeySysClk}
import sifive.blocks.devices.uart._
import sifive.fpgashells.clocks._
import sifive.fpgashells.ip.pango._
import sifive.fpgashells.ip.pango.ddr3.PGL22GMIGIODDRBase
import sifive.fpgashells.shell._
import sifive.fpgashells.shell.pango.{ChipLinkPGL22GPlacedOverlay, PGL22GShellBasicOverlays}

class PGL22GFPGATestHarness(override implicit val p: Parameters) extends PGL22GShellBasicOverlays {

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

  /*** Connect/Generate clocks ***/

  // connect to the PLL that will generate multiple clocks
  val harnessSysPLL = dp(PLLFactoryKey)()
  harnessSysPLL := sysClkNode

  // create and connect to the dutClock
  println(s"PGL22G FPGA Base Clock Freq: ${dp(DefaultClockFrequencyKey)} MHz")
  val dutClock = ClockSinkNode(freqMHz = dp(DefaultClockFrequencyKey))
  val dutWrangler = LazyModule(new ResetWrangler)
  val dutGroup = ClockGroup()
  dutClock := dutWrangler.node := dutGroup := harnessSysPLL
  // DOC include end: ClockOverlay

  /*** UART ***/

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

  /*** DDR ***/

  // val ddrNode = dp(DDROverlayKey).head.place(DDRDesignInput(dp(ExtTLMem).get.master.base, dutWrangler.node, harnessSysPLL)).overlayOutput.ddr
  val ddrNode = dp(DDROverlayKeySysClk).head.place(DDRDesignInputSysClk(dp(ExtTLMem).get.master.base, dutWrangler.node, sysClkNode)).overlayOutput.mig.node

  // connect 1 mem. channel to the FPGA DDR
  val inParams = topDesign match { case td: ChipTop =>
    td.lazySystem match { case lsys: CanHaveMasterTLMemPort =>
      lsys.memTLNode.edges.in(0)
    }
  }
  val ddrClient = TLClientNode(Seq(inParams.master))
  ddrNode := ddrClient

  // module implementation
  override lazy val module = new PGL22GFPGATestHarnessImp(this)
}

class PGL22GAXIFPGATestHarness(override implicit val p: Parameters) extends PGL22GShellBasicOverlays {
  def dp = designParameters
  val topDesign = LazyModule(p(BuildTop)(dp)).suggestName("chiptop")
  // DOC include start: ClockOverlay
  // place all clocks in the shell
  require(dp(ClockInputOverlayKey).size >= 1)
  val sysClkNode = dp(ClockInputOverlayKey)(0).place(ClockInputDesignInput()).overlayOutput.node
  /*** Connect/Generate clocks ***/
  // connect to the PLL that will generate multiple clocks
  val harnessSysPLL = dp(PLLFactoryKey)()
  harnessSysPLL := sysClkNode
  // create and connect to the dutClock
  println(s"PGL22G FPGA Base Clock Freq: ${dp(DefaultClockFrequencyKey)} MHz")
  val dutClock = ClockSinkNode(freqMHz = dp(DefaultClockFrequencyKey))
  val dutWrangler = LazyModule(new ResetWrangler)
  val dutGroup = ClockGroup()
  dutClock := dutWrangler.node := dutGroup := harnessSysPLL
  val io_uart_bb = BundleBridgeSource(() => (new UARTPortIO(dp(PeripheryUARTKey).head)))
  dp(UARTOverlayKey).head.place(UARTDesignInput(io_uart_bb))
  // module implementation
  override lazy val module = new PGL22GAXIFPGATestHarnessImp(this)
}

class PGL22GAXIFPGATestHarnessImp(_outer: PGL22GAXIFPGATestHarness) extends LazyRawModuleImp(_outer) with HasHarnessSignalReferences {

  val pgl22gOuter = _outer

  // is resetN
  val reset = IO(Input(Bool()))
  _outer.xdc.addPackagePin(reset, "L19")
  _outer.xdc.addIOStandard(reset, "LVCMOS12")

  val resetIBUF = Module(new GTP_INBUF)
  resetIBUF.io.I := reset

  val sysclk: Clock = _outer.sysClkNode.out.head._1.clock

  val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
  _outer.sdc.addAsyncPath(Seq(powerOnReset))

  val ereset: Bool = _outer.chiplink.get() match {
    case Some(x: ChipLinkPGL22GPlacedOverlay) => !x.ereset_n
    case _ => false.B
  }

  _outer.pllReset := ((!resetIBUF.io.O) || powerOnReset || ereset)

  // reset setup
  val hReset = Wire(Reset())
  hReset := _outer.dutClock.in.head._1.reset

  val buildtopClock = _outer.dutClock.in.head._1.clock
  val buildtopReset = WireInit(hReset)
  val dutReset = hReset.asAsyncReset
  val success = false.B

  childClock := buildtopClock
  childReset := buildtopReset

  // harness binders are non-lazy
  _outer.topDesign match { case d: HasIOBinders =>
    ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }

  // check the top-level reference clock is equal to the default
  // non-exhaustive since you need all ChipTop clocks to equal the default
  require(getRefClockFreq == p(DefaultClockFrequencyKey))
}

class PGL22GFPGATestHarnessImp(_outer: PGL22GFPGATestHarness) extends LazyRawModuleImp(_outer) with HasHarnessSignalReferences {

  val pgl22gOuter = _outer

  // is resetN
  val reset = IO(Input(Bool()))
  _outer.xdc.addPackagePin(reset, "L19")
  _outer.xdc.addIOStandard(reset, "LVCMOS12")

  val resetIBUF = Module(new GTP_INBUF)
  resetIBUF.io.I := reset

  val sysclk: Clock = _outer.sysClkNode.out.head._1.clock

  val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
  _outer.sdc.addAsyncPath(Seq(powerOnReset))

  val ereset: Bool = _outer.chiplink.get() match {
    case Some(x: ChipLinkPGL22GPlacedOverlay) => !x.ereset_n
    case _ => false.B
  }

  _outer.pllReset := ((!resetIBUF.io.O) || powerOnReset || ereset)

  // reset setup
  val hReset = Wire(Reset())
  hReset := _outer.dutClock.in.head._1.reset

  val buildtopClock = _outer.dutClock.in.head._1.clock
  val buildtopReset = WireInit(hReset)
  val dutReset = hReset.asAsyncReset
  val success = false.B

  childClock := buildtopClock
  childReset := buildtopReset

  // harness binders are non-lazy
  _outer.topDesign match { case d: HasIOBinders =>
    ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }

  // check the top-level reference clock is equal to the default
  // non-exhaustive since you need all ChipTop clocks to equal the default
  require(getRefClockFreq == p(DefaultClockFrequencyKey))
}

import chipyard.TestHarness
class PGL22GSimTestHarness(implicit p: Parameters) extends TestHarness

case object PGL22GBuildTop extends Field[Parameters => LazyModule]((p: Parameters) => new ChipTop()(p))

class PGL22GBareTestHarness(override implicit val p: Parameters) extends PGL22GShellBasicOverlays {
  def dp = designParameters
  val topDesign = LazyModule(p(BuildTop)(dp)).suggestName("chiptop")
  require(dp(ClockInputOverlayKey).size >= 1)
  val sysClkNode = dp(ClockInputOverlayKey)(0).place(ClockInputDesignInput()).overlayOutput.node
  val migUIClock = PLLNode(feedback = false)
  // val harnessSysPLL = dp(PLLFactoryKey)()
  // harnessSysPLL := sysClkNode
  migUIClock := sysClkNode
  println(s"PGL22G FPGA Base Clock Freq: ${dp(DefaultClockFrequencyKey)} MHz")
  val dutClock = ClockSinkNode(freqMHz = dp(DefaultClockFrequencyKey))
  val dutWrangler = LazyModule(new ResetWrangler)
  val dutGroup = ClockGroup()
  // dutClock := dutWrangler.node := dutGroup := harnessSysPLL
  dutClock := dutWrangler.node := dutGroup := migUIClock
  val io_uart_bb = BundleBridgeSource(() => (new UARTPortIO(dp(PeripheryUARTKey).head)))
  dp(UARTOverlayKey).head.place(UARTDesignInput(io_uart_bb))
  // val innerDDRIO = new PGL22GMIGIODDRBase
  override lazy val module = new PGL22GBareTestHarnessImp(this)
}

class PGL22GBareTestHarnessImp(_outer: PGL22GBareTestHarness) extends LazyRawModuleImp(_outer) with HasHarnessSignalReferences {
  val pgl22gOuter = _outer
  // is resetN
  val reset = IO(Input(Bool()))
  _outer.xdc.addPackagePin(reset, "L19")
  _outer.xdc.addIOStandard(reset, "LVCMOS12")
  val resetIBUF = Module(new GTP_INBUF)
  resetIBUF.io.I := reset
  val hardResetN = (!resetIBUF.io.O).asBool
  val sysclk: Clock = _outer.sysClkNode.out.head._1.clock
  val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
  _outer.sdc.addAsyncPath(Seq(powerOnReset))
  val ereset: Bool = _outer.chiplink.get() match {
    case Some(x: ChipLinkPGL22GPlacedOverlay) => !x.ereset_n
    case _ => false.B
  }
  // used for
  _outer.pllReset := (hardResetN || powerOnReset || ereset)
  // reset setup
  val hReset = Wire(Reset())
  hReset := _outer.dutClock.in.head._1.reset
  val buildtopClock = _outer.dutClock.in.head._1.clock
  val buildtopReset = WireInit(hReset)
  val dutReset = hReset.asAsyncReset
  val success = false.B
  childClock := buildtopClock
  childReset := buildtopReset

  val ddr = IO(new PGL22GMIGIODDRBase)

  // harness binders are non-lazy
  _outer.topDesign match { case d: HasIOBinders =>
    ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }
  // check the top-level reference clock is equal to the default
  // non-exhaustive since you need all ChipTop clocks to equal the default
  require(getRefClockFreq == p(DefaultClockFrequencyKey))

  // val ddrIO = IO(new PGL22GMIGIODDRBase)
  // BoringUtils.addSink(ddrIO.pad_addr_ch0, "pad_addr_ch0")
  // BoringUtils.addSink(ddrIO.pad_ba_ch0, "pad_ba_ch0")
  // BoringUtils.addSink(ddrIO.pad_rasn_ch0, "pad_rasn_ch0")
  // BoringUtils.addSink(ddrIO.pad_casn_ch0, "pad_casn_ch0")
  // BoringUtils.addSink(ddrIO.pad_wen_ch0, "pad_wen_ch0")
  // BoringUtils.addSink(ddrIO.pad_rstn_ch0, "pad_rstn_ch0")
  // BoringUtils.addSink(ddrIO.pad_ddr_clk_w, "pad_ddr_clk_w")
  // BoringUtils.addSink(ddrIO.pad_ddr_clkn_w, "pad_ddr_clkn_w")
  // BoringUtils.addSink(ddrIO.pad_cke_ch0, "pad_cke_ch0")
  // BoringUtils.addSink(ddrIO.pad_csn_ch0, "pad_csn_ch0")
  // BoringUtils.addSink(ddrIO.pad_dm_rdqs_ch0, "pad_dm_rdqs_ch0")
  // BoringUtils.addSink(ddrIO.pad_odt_ch0, "pad_odt_ch0")
  // BoringUtils.addSource(ddrIO.pad_loop_in, "pad_loop_in")
  // BoringUtils.addSource(ddrIO.pad_loop_in_h, "pad_loop_in_h")
  // BoringUtils.addSink(ddrIO.pad_loop_out, "pad_loop_out")
  // BoringUtils.addSink(ddrIO.pad_loop_out_h, "pad_loop_out_h")

  // inout IOs
  // BoringUtils.addSource(ddrIO.pad_dq_ch0, "pad_dq_ch0", disableDedup = true)
  // BoringUtils.addSource(ddrIO.pad_dqsn_ch0, "pad_dqsn_ch0")
  // BoringUtils.addSource(ddrIO.pad_dqs_ch0, "pad_dqs_ch0")

  // AnalogUtils.add(ddrIO.pad_dq_ch0, "pad_dq_ch0")
  // AnalogUtils.add(ddrIO.pad_dqsn_ch0, "pad_dqsn_ch0")
  // AnalogUtils.add(ddrIO.pad_dqs_ch0, "pad_dqs_ch0")

  // BoringUtils.addSource(sysclk, "pll_refclk_in")
  // BoringUtils.addSource(hardResetN, "top_rst_n")
  // BoringUtils.addSource(hardResetN, "ddrc_rst")

  // BoringUtils.addSink(_outer.migUIClock.out.head._1.member.head.clock, "pll_aclk_2")
  val ddrphy_rst_done = WireInit(false.B)
  val ddrc_init_done = WireInit(false.B)
  val pll_lock = WireInit(false.B)
  // BoringUtils.addSink(ddrphy_rst_done, "ddrphy_rst_done")
  // BoringUtils.addSink(ddrc_init_done, "ddrc_init_done")
  // BoringUtils.addSink(pll_lock, "pll_lock")
  _outer.migUIClock.out.head._1.member.head.reset := !pll_lock
}

class PGL22GTestHarness(override implicit val p: Parameters) extends PGL22GShellBasicOverlays {

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

  /*** Connect/Generate clocks ***/

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

  /*** UART ***/

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

class PGL22GTestHarnessImp(_outer: PGL22GTestHarness) extends LazyRawModuleImp(_outer) with HasHarnessSignalReferences {
  val pgl22gOuter = _outer
  // is resetN
  val reset = IO(Input(Bool()))
  _outer.xdc.addPackagePin(reset, "L19")
  _outer.xdc.addIOStandard(reset, "LVCMOS12")
  val resetIBUF = Module(new GTP_INBUF)
  resetIBUF.io.I := reset
  val sysclk: Clock = _outer.sysClkNode.out.head._1.clock
  val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
  _outer.sdc.addAsyncPath(Seq(powerOnReset))
  val ereset: Bool = _outer.chiplink.get() match {
    case Some(x: ChipLinkPGL22GPlacedOverlay) => !x.ereset_n
    case _ => false.B
  }
  // used for
  _outer.pllReset := ((!resetIBUF.io.O) || powerOnReset || ereset)
  // reset setup
  val hReset = Wire(Reset())
  hReset := _outer.dutClock.in.head._1.reset
  val buildtopClock = _outer.dutClock.in.head._1.clock
  val buildtopReset = WireInit(hReset)
  val dutReset = hReset.asAsyncReset
  val success = false.B
  childClock := buildtopClock
  childReset := buildtopReset
  // harness binders are non-lazy
  _outer.topDesign match { case d: HasIOBinders =>
    ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }
  // check the top-level reference clock is equal to the default
  // non-exhaustive since you need all ChipTop clocks to equal the default
  require(getRefClockFreq == p(DefaultClockFrequencyKey))
}
