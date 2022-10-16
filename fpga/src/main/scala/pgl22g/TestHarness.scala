package chipyard.fpga.pgl22g

import chisel3._
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.config.Parameters
import chipyard.{BuildTop, HasHarnessSignalReferences}
import chipyard.harness.ApplyHarnessBinders
import chipyard.iobinders.HasIOBinders
import sifive.fpgashells.shell.pangogshell.PGL22GShell

class PGL22GFPGATestHarness(override implicit val p: Parameters) extends PGL22GShell with HasHarnessSignalReferences {

  val lazyDut = LazyModule(p(BuildTop)(p)).suggestName("chiptop")

  // Convert harness resets from Bool to Reset type.
  val hReset = Wire(Reset())
  hReset := ck_rst

  val dReset = Wire(AsyncReset())
  dReset := reset_core.asAsyncReset

  // default to 32MHz clock
  withClockAndReset(clock_32MHz, hReset) {
    val dut = Module(lazyDut.module)
  }

  val buildtopClock = clock_32MHz
  val buildtopReset = hReset
  val success = false.B

  val dutReset = dReset

  // must be after HasHarnessSignalReferences assignments
  lazyDut match { case d: HasIOBinders =>
    ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }
}

