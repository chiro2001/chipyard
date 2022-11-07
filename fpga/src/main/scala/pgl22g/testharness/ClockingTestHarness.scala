package pgl22g.testharness

import chipsalliance.rocketchip.config.Parameters
import chipyard.harness.ApplyHarnessBinders
import chipyard.iobinders.HasIOBinders
import chipyard.{BuildTop, DefaultClockFrequencyKey, HasHarnessSignalReferences}
import chisel3._
import freechips.rocketchip.diplomacy.{BundleBridgeSource, LazyModule, LazyRawModuleImp}
import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTPortIO}
import sifive.fpgashells.clocks.{ClockGroup, ClockSinkNode, PLLFactoryKey, PLLNode, ResetWrangler}
import sifive.fpgashells.ip.pango.ddr3.PGL22GMIGIODDRBase
import sifive.fpgashells.shell.pango.{ChipLinkPGL22GPlacedOverlay, PGL22GShellDDROverlays, SPIFlashIO}
import sifive.fpgashells.shell.{ClockInputDesignInput, ClockInputOverlayKey, UARTDesignInput, UARTOverlayKey}

class PGL22GClockingTestHarness(override implicit val p: Parameters) extends PGL22GShellDDROverlays {
  def dp = designParameters

  val topDesign = LazyModule(p(BuildTop)(dp)).suggestName("chiptop")
  require(dp(ClockInputOverlayKey).nonEmpty)
  val sysClkNode = dp(ClockInputOverlayKey).head.place(ClockInputDesignInput()).overlayOutput.node
  // val migUIClock = PLLNode(feedback = false)
  val harnessSysPLL = dp(PLLFactoryKey)()
  // harnessSysPLL.out.head._1.member.head.reset :=
  // harnessSysPLL.out.head._1.member.head.clock := pll_clk_bus
  harnessSysPLL := sysClkNode
  // migUIClock := sysClkNode
  // harnessSysPLL := migUIClock
  // migUIClock := harnessSysPLL
  println(s"PGL22G FPGA Base Clock Freq: ${dp(DefaultClockFrequencyKey)} MHz")
  val dutClock = ClockSinkNode(freqMHz = dp(DefaultClockFrequencyKey))
  // val pllInputClock = ClockSinkNode(freqMHz = 50.0)
  val dutWrangler = LazyModule(new ResetWrangler)
  // val dutWranglerPLL = LazyModule(new ResetWrangler)
  val dutGroup = ClockGroup()
  // val migGroup = ClockGroup()
  dutClock := dutWrangler.node := dutGroup := harnessSysPLL
  // pllInputClock := dutWranglerPLL.node := migGroup := migUIClock
  // harnessSysPLL := dutWranglerPLL.node := migGroup := migUIClock
  // harnessSysPLL := pllInputClock
  // harnessSysPLL.out.head._1.member.head <> migUIClock.in.head._1
  val io_uart_bb = BundleBridgeSource(() => (new UARTPortIO(dp(PeripheryUARTKey).head)))
  dp(UARTOverlayKey).head.place(UARTDesignInput(io_uart_bb))
  // val innerDDRIO = new PGL22GMIGIODDRBase
  override lazy val module = new PGL22GClockingTestHarnessImp(this)
}

class PGL22GClockingTestHarnessImp(_outer: PGL22GClockingTestHarness)
  extends LazyRawModuleImp(_outer)
    with HasHarnessSignalReferences
    with PGL22GTestHarnessDDRImp
    with PGL22GTestHarnessUartImp
    with PGL22GTestHarnessSPIFlashImpl {
  val pgl22gOuter = _outer
  override val uart = _outer.io_uart_bb.bundle
  override val qspi = IO(new SPIFlashIO)
  // is resetN
  val reset = IO(Input(Bool())).suggestName("reset")
  _outer.fdc.addPackagePin(reset, "L19")
  _outer.fdc.addIOStandard(reset, "LVCMOS12")
  // val resetIBUF = Module(new GTP_INBUF)
  // resetIBUF.io.I := reset
  // val hardResetN = (resetIBUF.io.O).asBool
  val hardResetN = reset
  val sysclk: Clock = _outer.sysClkNode.out.head._1.clock
  // val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
  val powerOnReset: Bool = false.B
  _outer.sdc.addAsyncPath(Seq(powerOnReset))
  val ereset: Bool = _outer.chiplink.get() match {
    case Some(x: ChipLinkPGL22GPlacedOverlay) => !x.ereset_n
    case _ => false.B
  }
  // used for
  _outer.pllReset := (!hardResetN || powerOnReset || ereset)
  // reset setup
  val hReset = Wire(Reset())
  hReset := _outer.dutClock.in.head._1.reset
  val buildtopClock = _outer.dutClock.in.head._1.clock
  val buildtopReset = WireInit(hReset)
  val dutReset = hReset.asAsyncReset
  val success = WireInit(false.B)
  childClock := buildtopClock
  childReset := buildtopReset

  val ddr = IO(new PGL22GMIGIODDRBase)

  val ddrphy_rst_done = WireInit(false.B)
  val ddrc_init_done = WireInit(false.B)
  val pll_lock = WireInit(false.B)
  val pll_clk_bus = WireInit(sysclk)
  // _outer.migUIClock.out.head._1.member.head.reset := (!(pll_lock & ddrc_init_done & ddrphy_rst_done)) || _outer.pllReset
  // _outer.migUIClock.out.head._1.member.head.clock := pll_clk_bus
  // _outer.harnessSysPLL.out.head._1.member.head <> _outer.migUIClock.in.head._1
  // harness binders are non-lazy
  _outer.topDesign match {
    case d: HasIOBinders =>
      ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }
  require(getRefClockFreq == p(DefaultClockFrequencyKey), s"require freq: ${p(DefaultClockFrequencyKey)}")
}