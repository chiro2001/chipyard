package pgl22g.testharness

import chipsalliance.rocketchip.config.Parameters
import chipyard.{BuildTop, DefaultClockFrequencyKey, HasHarnessSignalReferences}
import chipyard.harness.ApplyHarnessBinders
import chipyard.iobinders.HasIOBinders
import chisel3._
import freechips.rocketchip.diplomacy.{LazyModule, LazyRawModuleImp}
import sifive.fpgashells.ip.pango.{GTP_INBUF, PowerOnResetFPGAOnly}
import sifive.fpgashells.ip.pango.ddr3.PGL22GMIGIODDRBase
import sifive.fpgashells.shell.pango.{ChipLinkPGL22GPlacedOverlay, PGL22GPerfShell}

class PGL22GTestHarnessImp(_outer: PGL22GTestHarness)
  extends LazyRawModuleImp(_outer)
    with HasHarnessSignalReferences
    with PGL22GTestHarnessUartImp {
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
  val success = WireInit(false.B)
  childClock := buildtopClock
  childReset := buildtopReset
  // harness binders are non-lazy
  _outer.topDesign match {
    case d: HasIOBinders =>
      ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }
  // check the top-level reference clock is equal to the default
  // non-exhaustive since you need all ChipTop clocks to equal the default
  require(getRefClockFreq == p(DefaultClockFrequencyKey))
  override val uart = _outer.io_uart_bb.bundle
}

class PGL22GPerfTestHarness(override implicit val p: Parameters)
  extends PGL22GPerfShell
    with HasHarnessSignalReferences
    with PGL22GTestHarnessDDRImp
    with PGL22GTestHarnessPerfUartImp {
  val lazyDut = LazyModule(p(BuildTop)(p)).suggestName("chiptop")

  val sysclk = Wire(Clock())
  sysclk <> sys_clock
  val hardResetN = WireInit(!reset)
  val ddrphy_rst_done = WireInit(false.B)
  val ddrc_init_done = WireInit(false.B)
  val pll_lock = WireInit(false.B)
  val pll_clk_bus = Wire(Clock())

  // Convert harness resets from Bool to Reset type.
  val hReset = Wire(Reset())
  hReset := ~hardResetN

  // val dReset = Wire(AsyncReset())
  // dReset := reset_core.asAsyncReset

  withClockAndReset(pll_clk_bus, hReset) {
    val dut = Module(lazyDut.module)
  }

  val ddr: PGL22GMIGIODDRBase = IO(new PGL22GMIGIODDRBase)
  val buildtopClock = pll_clk_bus
  val buildtopReset = hReset

  val success = WireInit(false.B)

  // val dutReset = dReset
  val dutReset = hReset

  // must be after HasHarnessSignalReferences assignments
  lazyDut match {
    case d: HasIOBinders =>
      ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }
}