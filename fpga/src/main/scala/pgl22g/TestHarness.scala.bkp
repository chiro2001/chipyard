package chipyard.fpga.pgl22g

import chisel3._
import freechips.rocketchip.diplomacy.{LazyModule, LazyRawModuleImp}
import freechips.rocketchip.config.Parameters
import chipyard.{BuildTop, DefaultClockFrequencyKey, HasHarnessSignalReferences}
import chipyard.harness.ApplyHarnessBinders
import chipyard.iobinders.HasIOBinders
import sifive.fpgashells.ip.xilinx.{IBUF, PowerOnResetFPGAOnly}
import sifive.fpgashells.shell.pangogshell.{NegativeResetWrapper, PGL22GShell}

class PGL22GFPGATestHarness(override implicit val p: Parameters) extends PGL22GShell with HasHarnessSignalReferences {

  val lazyDut = LazyModule(p(BuildTop)(p)).suggestName("chiptop")

  // Convert harness resets from Bool to Reset type.
  val hReset = Wire(Reset())
  hReset := ~ck_rst

  val dReset = Wire(AsyncReset())
  dReset := reset_core.asAsyncReset

  val clockUse = clock_8MHz

  // default to 32MHz clock
  withClockAndReset(clockUse, hReset) {
    // val dut = Module(new NegativeResetWrapper(lazyDut.module, moduleName = "ChipTopWrapper"))
    val dut = Module(lazyDut.module)
  }

  val buildtopClock = clockUse
  val buildtopReset = hReset
  val success = false.B

  val dutReset = dReset

  // must be after HasHarnessSignalReferences assignments
  lazyDut match { case d: HasIOBinders =>
    ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }
}

// class PGL22GFPGATestHarnessImp(_outer: PGL22GFPGATestHarness) extends LazyRawModuleImp(_outer) with HasHarnessSignalReferences {
//
//   val pgl22gOuter = _outer
//
//   val reset = IO(Input(Bool()))
//
//   val resetIBUF = Module(new IBUF)
//   resetIBUF.io.I := reset
//
//   val sysclk: Clock = _outer.sysClkNode.out.head._1.clock
//
//   val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
//   _outer.sdc.addAsyncPath(Seq(powerOnReset))
//
//   val ereset: Bool = _outer.chiplink.get() match {
//     case Some(x: ChipLinkPGL22GPlacedOverlay) => !x.ereset_n
//     case _ => false.B
//   }
//
//   _outer.pllReset := (resetIBUF.io.O || powerOnReset || ereset)
//
//   // reset setup
//   val hReset = Wire(Reset())
//   hReset := _outer.dutClock.in.head._1.reset
//
//   val buildtopClock = _outer.dutClock.in.head._1.clock
//   val buildtopReset = WireInit(hReset)
//   val dutReset = hReset.asAsyncReset
//   val success = false.B
//
//   childClock := buildtopClock
//   childReset := buildtopReset
//
//   // harness binders are non-lazy
//   _outer.topDesign match { case d: HasIOBinders =>
//     ApplyHarnessBinders(this, d.lazySystem, d.portMap)
//   }
//
//   // check the top-level reference clock is equal to the default
//   // non-exhaustive since you need all ChipTop clocks to equal the default
//   require(getRefClockFreq == p(DefaultClockFrequencyKey))
// }
