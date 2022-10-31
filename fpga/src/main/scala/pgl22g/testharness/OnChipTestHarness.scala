package pgl22g.testharness

import chipsalliance.rocketchip.config.Parameters
import chipyard.harness.ApplyHarnessBinders
import chipyard.iobinders.{HasIOBinders, JTAGChipIO}
import chipyard.{BuildTop, ChipTop, DefaultClockFrequencyKey, HasHarnessSignalReferences}
import chisel3._
import freechips.rocketchip.diplomacy.{BundleBridgeSource, InModuleBody, LazyModule, LazyRawModuleImp}
import pgl22g.onchip.OnChipDigitalTop
import shell.pango.PGL22GOnChipShell
import sifive.blocks.devices.uart.{PeripheryUARTKey, UARTPortIO}
import sifive.fpgashells.clocks.{ClockGroup, ClockSinkNode, PLLFactoryKey, ResetWrangler}
import sifive.fpgashells.ip.pango.{GTP_INBUF, PowerOnResetFPGAOnly}
import sifive.fpgashells.shell.pango.{ChipLinkPGL22GPlacedOverlay, SPIFlashIO}
import sifive.fpgashells.shell.{ClockInputDesignInput, ClockInputOverlayKey, UARTDesignInput, UARTOverlayKey}
import vexriscv.chipyard.{CoreInternalJTAGDebugKey, VexJTAGChipIO}

class PGL22GOnChipTestHarness(override implicit val p: Parameters) extends PGL22GOnChipShell {
  def dp = designParameters

  val topDesign = LazyModule(p(BuildTop)(dp)).suggestName("chiptop")
  require(dp(ClockInputOverlayKey).nonEmpty)
  val sysClkNode = dp(ClockInputOverlayKey).head.place(ClockInputDesignInput()).overlayOutput.node
  val harnessSysPLL = dp(PLLFactoryKey)()
  harnessSysPLL := sysClkNode
  println(s"PGL22G FPGA Base Clock Freq: ${dp(DefaultClockFrequencyKey)} MHz")
  val dutClock = ClockSinkNode(freqMHz = dp(DefaultClockFrequencyKey))
  val dutWrangler = LazyModule(new ResetWrangler)
  val dutGroup = ClockGroup()
  dutClock := dutWrangler.node := dutGroup := harnessSysPLL
  val io_uart_bb = BundleBridgeSource(() => (new UARTPortIO(dp(PeripheryUARTKey).head)))
  dp(UARTOverlayKey).head.place(UARTDesignInput(io_uart_bb))
  // val io_jtag = BundleBridgeSource(() => (new VexJTAGChipIO))
  override lazy val module = new PGL22GOnChipTestHarnessImp(this)
}

class PGL22GOnChipTestHarnessImp(_outer: PGL22GOnChipTestHarness)
  extends LazyRawModuleImp(_outer)
    with HasHarnessSignalReferences
    with PGL22GTestHarnessUartTopClockImp
    with PGL22GTestHarnessJtagImpl
    with PGL22GTestHarnessSPIFlashImpl {
  val pgl22gOuter = _outer
  override val uart = _outer.io_uart_bb.bundle
  override val jtag = IO(new JTAGChipIO)
  // // jtag.TCK <> _outer.io_jtag.bundle.jtag_tck
  // // jtag.TMS <> _outer.io_jtag.bundle.jtag_tms
  // // jtag.TDO <> _outer.io_jtag.bundle.jtag_tdo
  // // jtag.TDI <> _outer.io_jtag.bundle.jtag_tdi
  // _outer.topDesign match {
  //   case top: ChipTop =>
  //     println(s"ChipTop: ${top}")
  //     top.lazySystem match {
  //       case sys: OnChipDigitalTop =>
  //         println(s"topSystem: ${sys}")
  //         sys.jtagBundle.get.jtag_tck <> jtag.TCK
  //         sys.jtagBundle.get.jtag_tms <> jtag.TMS
  //         sys.jtagBundle.get.jtag_tdo <> jtag.TDO
  //         sys.jtagBundle.get.jtag_tdi <> jtag.TDI
  //     }
  // }
  override val qspi = IO(new SPIFlashIO)
  // is resetN
  val reset = IO(Input(Bool()))
  _outer.fdc.addPackagePin(reset, "L19")
  _outer.fdc.addIOStandard(reset, "LVCMOS12")
  val resetIBUF = Module(new GTP_INBUF)
  resetIBUF.io.I := reset
  val hardResetN = (resetIBUF.io.O).asBool
  val sysclk: Clock = _outer.sysClkNode.out.head._1.clock
  val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
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

  _outer.topDesign match {
    case d: HasIOBinders =>
      ApplyHarnessBinders(this, d.lazySystem, d.portMap)
  }
  require(getRefClockFreq == p(DefaultClockFrequencyKey))
}