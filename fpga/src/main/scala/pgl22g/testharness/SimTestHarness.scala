package pgl22g.testharness

import chipsalliance.rocketchip.config.Parameters
import chipyard.{BuildTop, HarnessClockInstantiatorKey, HasHarnessSignalReferences}
import chipyard.harness.ApplyHarnessBinders
import chipyard.iobinders.HasIOBinders
import chisel3._
import freechips.rocketchip.diplomacy.{LazyModule, LazyRawModuleImp}
import freechips.rocketchip.prci.{ClockBundle, ClockBundleParameters}
import sifive.fpgashells.shell.{IOShell, SDC}

class PGL22GSimShell(implicit p: Parameters) extends IOShell {
  val sdc = new SDC("shell.sdc")
}

class PGL22GSimTestHarness(implicit p: Parameters) extends PGL22GSimShell {
  def dp = designParameters

  val topDesign = LazyModule(p(BuildTop)(dp)).suggestName("chiptop")

  // val io_uart_bb = BundleBridgeSource(() => new UARTPortIO(dp(PeripheryUARTKey).head))
  // dp(UARTOverlayKey).head.place(UARTDesignInput(io_uart_bb))

  override lazy val module = new PGL22GSimTestHarnessImpl(this)
}

class PGL22GSimTestHarnessImpl(_outer: PGL22GSimTestHarness)
  extends LazyRawModuleImp(_outer)
    with HasHarnessSignalReferences {
  val io_success = IO(Output(Bool()))
  io_success := false.B

  val reset = IO(Input(Bool()))
  val clock = IO(Input(Clock()))

  val resetInner = WireInit(false.B.asTypeOf(reset.cloneType))
  val clockInner = WireInit(false.B.asClock)

  override def buildtopClock = clockInner

  override def buildtopReset = resetInner

  override def dutReset = resetInner

  override def success = io_success

  val refClkBundle = p(HarnessClockInstantiatorKey).requestClockBundle("buildtop_reference_clock", getRefClockFreq * (1000 * 1000))
  val implicitHarnessClockBundle = Wire(new ClockBundle(ClockBundleParameters()))
  implicitHarnessClockBundle.clock := clock
  implicitHarnessClockBundle.reset := reset
  p(HarnessClockInstantiatorKey).instantiateHarnessDividerPLL(implicitHarnessClockBundle)

  // buildtopClock := refClkBundle.clock
  // buildtopReset := WireInit(refClkBundle.reset)

  withClockAndReset(buildtopClock, buildtopReset) {
    _outer.topDesign match {
      case d: HasIOBinders =>
        ApplyHarnessBinders(this, d.lazySystem, d.portMap)
    }
  }

  buildtopClock := refClkBundle.clock
  buildtopReset := WireInit(refClkBundle.reset)
}