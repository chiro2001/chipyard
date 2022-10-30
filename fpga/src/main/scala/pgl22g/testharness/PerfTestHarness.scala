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