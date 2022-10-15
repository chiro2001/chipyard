package chipyard.fpga.pgl22g.bringup

import chisel3._
import chisel3.experimental.attach
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.config.{Field, Parameters}
import freechips.rocketchip.tilelink.{TLAsyncCrossingSink, TLInwardNode}
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.shell.xilinx._
import sifive.fpgashells.clocks._
import sifive.fpgashells.devices.pango.ddr3.{PangoPGL22GMIG, PangoPGL22GMIGPads, PangoPGL22GMIGParams}
import testchipip.TSIHostWidgetIO
import chipyard.fpga.pgl22g.FMCPMap
import sifive.fpgashells.shell.pango.UARTPangoPlacedOverlay
import sifive.fpgashells.shell.pango.pgl22gshell.PGL22GShellBasicOverlays

/* Connect the I2C to certain FMC pins */
// class BringupI2CPGL22GPlacedOverlay(val shell: PGL22GShellBasicOverlays, name: String, val designInput: I2CDesignInput, val shellInput: I2CShellInput)
//   extends I2CPangoPlacedOverlay(name, designInput, shellInput)
// {
//   shell { InModuleBody {
//     require(shellInput.index == 0) // only support 1 I2C <-> FMC connection
//     val i2cLocations = List(List(FMCPMap("K11"), FMCPMap("E2")))
//     val packagePinsWithPackageIOs = Seq((i2cLocations(shellInput.index)(0), IOPin(io.scl)),
//                                         (i2cLocations(shellInput.index)(1), IOPin(io.sda)))
//
//     packagePinsWithPackageIOs foreach { case (pin, io) => {
//       shell.xdc.addPackagePin(io, pin)
//       shell.xdc.addIOStandard(io, "LVCMOS18")
//       shell.xdc.addIOB(io)
//     } }
//   } }
// }

// class BringupI2CPGL22GShellPlacer(val shell: PGL22GShellBasicOverlays, val shellInput: I2CShellInput)(implicit val valName: ValName)
//   extends I2CShellPlacer[PGL22GShellBasicOverlays]
// {
//   def place(designInput: I2CDesignInput) = new BringupI2CPGL22GPlacedOverlay(shell, valName.name, designInput, shellInput)
// }

/* Connect the UART to certain FMC pins */
class BringupUARTPGL22GPlacedOverlay(val shell: PGL22GShellBasicOverlays, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTPangoPlacedOverlay(name, designInput, shellInput, true)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq((FMCPMap("E9"), IOPin(io.ctsn.get)), // unused
                                        (FMCPMap("E10"), IOPin(io.rtsn.get)), // unused
                                        (FMCPMap("C15"), IOPin(io.rxd)),
                                        (FMCPMap("C14"), IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }

    // add pullup on ctsn (ctsn is an input that is not used or driven)
    packagePinsWithPackageIOs take 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}

class BringupUARTPGL22GShellPlacer(shell: PGL22GShellBasicOverlays, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[PGL22GShellBasicOverlays] {
  def place(designInput: UARTDesignInput) = new BringupUARTPGL22GPlacedOverlay(shell, valName.name, designInput, shellInput)
}

/* Connect GPIOs to FPGA I/Os */
// abstract class GPIOPangoPlacedOverlay(name: String, di: GPIODesignInput, si: GPIOShellInput)
//   extends GPIOPlacedOverlay(name, di, si)
// {
//   def shell: PangoShell
//
//   shell { InModuleBody {
//     (io.gpio zip tlgpioSink.bundle.pins).map { case (ioPin, sinkPin) =>
//       val iobuf = Module(new IOBUF)
//       iobuf.suggestName(s"gpio_iobuf")
//       attach(ioPin, iobuf.io.IO)
//       sinkPin.i.ival := iobuf.io.O
//       iobuf.io.T := !sinkPin.o.oe
//       iobuf.io.I := sinkPin.o.oval
//     }
//   } }
// }

// class BringupGPIOPGL22GPlacedOverlay(val shell: PGL22GShellBasicOverlays, name: String, val designInput: GPIODesignInput, val shellInput: GPIOShellInput, gpioNames: Seq[String])
//    extends GPIOPangoPlacedOverlay(name, designInput, shellInput)
// {
//   shell { InModuleBody {
//     require(gpioNames.length == io.gpio.length)
//
//     val packagePinsWithIOStdWithPackageIOs = (gpioNames zip io.gpio).map { case (name, io) =>
//       val (pin, iostd, pullupEnable) = BringupGPIOs.pinMapping(name)
//       (pin, iostd, pullupEnable, IOPin(io))
//     }
//
//     packagePinsWithIOStdWithPackageIOs foreach { case (pin, iostd, pullupEnable, io) => {
//       shell.xdc.addPackagePin(io, pin)
//       shell.xdc.addIOStandard(io, iostd)
//       if (iostd == "LVCMOS12") { shell.xdc.addDriveStrength(io, "8") }
//       if (pullupEnable) { shell.xdc.addPullup(io) }
//     } }
//   } }
// }
//
// class BringupGPIOPGL22GShellPlacer(shell: PGL22GShellBasicOverlays, val shellInput: GPIOShellInput, gpioNames: Seq[String])(implicit val valName: ValName)
//   extends GPIOShellPlacer[PGL22GShellBasicOverlays] {
//   def place(designInput: GPIODesignInput) = new BringupGPIOPGL22GPlacedOverlay(shell, valName.name, designInput, shellInput, gpioNames)
// }

case class TSIHostShellInput()
case class TSIHostDesignInput(
  serialIfWidth: Int,
  node: BundleBridgeSource[TSIHostWidgetIO]
  )(
  implicit val p: Parameters)
case class TSIHostOverlayOutput()
trait TSIHostShellPlacer[Shell] extends ShellPlacer[TSIHostDesignInput, TSIHostShellInput, TSIHostOverlayOutput]

case object TSIHostOverlayKey extends Field[Seq[DesignPlacer[TSIHostDesignInput, TSIHostShellInput, TSIHostOverlayOutput]]](Nil)

abstract class TSIHostPlacedOverlay[IO <: Data](val name: String, val di: TSIHostDesignInput, val si: TSIHostShellInput)
  extends IOPlacedOverlay[IO, TSIHostDesignInput, TSIHostShellInput, TSIHostOverlayOutput]
{
  implicit val p = di.p
}

case object TSIHostPGL22GDDRSize extends Field[BigInt](0x40000000L * 2) // 2GB
class TSIHostPGL22GPlacedOverlay(val shell: BringupPGL22GFPGATestHarness, name: String, val designInput: TSIHostDesignInput, val shellInput: TSIHostShellInput)
  extends TSIHostPlacedOverlay[TSIHostWidgetIO](name, designInput, shellInput)
{
  val tlTsiSerialSink = di.node.makeSink()
  val tsiIoNode = BundleBridgeSource(() => new TSIHostWidgetIO(di.serialIfWidth))
  val topTSIIONode = shell { tsiIoNode.makeSink() }

  def overlayOutput = TSIHostOverlayOutput()
  def ioFactory = new TSIHostWidgetIO(di.serialIfWidth)

  InModuleBody {
    // connect TSI serial
    val tsiSourcePort = tsiIoNode.bundle
    val tsiSinkPort = tlTsiSerialSink.bundle
    tsiSinkPort.serial_clock := tsiSourcePort.serial_clock
    tsiSourcePort.serial.out.bits := tsiSinkPort.serial.out.bits
    tsiSourcePort.serial.out.valid := tsiSinkPort.serial.out.valid
    tsiSinkPort.serial.out.ready := tsiSourcePort.serial.out.ready
    tsiSinkPort.serial.in.bits  := tsiSourcePort.serial.in.bits
    tsiSinkPort.serial.in.valid := tsiSourcePort.serial.in.valid
    tsiSourcePort.serial.in.ready := tsiSinkPort.serial.in.ready
  }
}

case object TSIClockMaxFrequencyKey extends Field[Int](50) // in MHz
class BringupTSIHostPGL22GPlacedOverlay(override val shell: BringupPGL22GFPGATestHarness, override val name: String, override val designInput: TSIHostDesignInput, override val shellInput: TSIHostShellInput)
  extends TSIHostPGL22GPlacedOverlay(shell, name, designInput, shellInput)
{
  // connect the TSI port
  shell { InModuleBody {
    // connect TSI signals
    val tsiPort = topTSIIONode.bundle
    io <> tsiPort

    require(di.serialIfWidth == 4)

    val clkIo = IOPin(io.serial_clock)
    val packagePinsWithPackageIOs = Seq(
      (FMCPMap("D8"), clkIo),
      (FMCPMap("D17"), IOPin(io.serial.out.ready)),
      (FMCPMap("D18"), IOPin(io.serial.out.valid)),
      (FMCPMap("D11"), IOPin(io.serial.out.bits, 0)),
      (FMCPMap("D12"), IOPin(io.serial.out.bits, 1)),
      (FMCPMap("D14"), IOPin(io.serial.out.bits, 2)),
      (FMCPMap("D15"), IOPin(io.serial.out.bits, 3)),
      (FMCPMap("D26"), IOPin(io.serial.in.ready)),
      (FMCPMap("D27"), IOPin(io.serial.in.valid)),
      (FMCPMap("D20"), IOPin(io.serial.in.bits, 0)),
      (FMCPMap("D21"), IOPin(io.serial.in.bits, 1)),
      (FMCPMap("D23"), IOPin(io.serial.in.bits, 2)),
      (FMCPMap("D24"), IOPin(io.serial.in.bits, 3)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
    } }

    // Don't add an IOB to the clock
    (packagePinsWithPackageIOs take 1) foreach { case (pin, io) => {
      shell.xdc.addIOB(io)
    } }

    shell.sdc.addClock("TSI_CLK", clkIo, p(TSIClockMaxFrequencyKey))
    shell.sdc.addGroup(pins = Seq(clkIo))
    shell.xdc.clockDedicatedRouteFalse(clkIo)
  } }
}

class BringupTSIHostPGL22GShellPlacer(shell: BringupPGL22GFPGATestHarness, val shellInput: TSIHostShellInput)(implicit val valName: ValName)
  extends TSIHostShellPlacer[BringupPGL22GFPGATestHarness] {
  def place(designInput: TSIHostDesignInput) = new BringupTSIHostPGL22GPlacedOverlay(shell, valName.name, designInput, shellInput)
}
