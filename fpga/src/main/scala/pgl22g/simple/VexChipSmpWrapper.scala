package pgl22g.simple

import chipsalliance.rocketchip.config.{Config, Field, Parameters}
import chisel3._
import freechips.rocketchip.amba.axi4.{AXI4MasterNode, AXI4MasterParameters, AXI4MasterPortParameters, AXI4SlaveNode, AXI4SlaveParameters, AXI4SlavePortParameters, AXI4Xbar}
import freechips.rocketchip.diplomacy.{AddressSet, LazyModule, LazyModuleImp, LazyModuleImpLike, LazyRawModuleImp, TransferSizes}
import vexriscv.chipyard._
import vexriscv.demo.VexOnChipConfig

import java.io.{File, PrintWriter}

case class VexChipSmpWrapperConfig
(cpuCount: Int = 2,
 cpuConfig: VexOnChipConfig = VexOnChipConfig.default)

object VexChipSmpWrapperConfig {
  def default = VexChipSmpWrapperConfig()
}

class VexChipSmpWrapper(implicit p: Parameters) extends LazyModule {
  val config = VexChipSmpWrapperConfig.default

  import config._

  val axiMasters = (0 until cpuCount).map(i =>
    AXI4MasterNode(Seq(AXI4MasterPortParameters(
      masters = Seq(AXI4MasterParameters("cpuBus" + i))))))
  val xbar = AXI4Xbar()
  val axiSlave = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address = List(AddressSet(0x80000000L, 0xFFFFFFFL)), // when truncated to 32-bits, is 0
      supportsWrite = TransferSizes(1, 8),
      supportsRead = TransferSizes(1, 8),
      interleavedId = Some(0),
    )),
    beatBytes = 8)))
  axiSlave := xbar
  axiMasters.foreach(master => xbar := master)

  override def module = new VexChipSmpWrapperImpl(this)
}

class VexChipSmpWrapperImpl(_outer: VexChipSmpWrapper) extends LazyRawModuleImp(_outer) {

  import _outer.config._

  require(cpuCount <= 4)
  val cpus = (0 until cpuCount).zip(Seq(
    () => new VexCore0(false, optionalConfig = Some(cpuConfig)),
    () => new VexCore1(false, optionalConfig = Some(cpuConfig)),
    () => new VexCore2(false, optionalConfig = Some(cpuConfig)),
    () => new VexCore3(false, optionalConfig = Some(cpuConfig)),
  )).map(c => {
    Module(c._2())
  })
  val externalInterrupt = false.B
  val timerInterrupt = false.B
  cpus.foreach(cpu => {
    cpu.io.externalInterrupt := externalInterrupt
    cpu.io.timerInterrupt := timerInterrupt
  })
  // try {
  //   new File("./sims/verilator/generated-src/test/").mkdirs()
  // } catch {
  //   case e: Exception =>
  //     e.printStackTrace()
  // }
  // val writer = new PrintWriter(new File("./sims/verilator/generated-src/test/test.rom.conf"))
  // writer.println("")
  // writer.close()
}

case object VexChipSmpKey extends Field[VexChipSmpWrapperConfig]

class WithVexChipSmpWrapperConfig extends Config((site, here, up) => {
  case VexChipSmpKey => VexChipSmpWrapperConfig.default
})

class VexChipSmpConfig extends Config(
  new WithVexChipSmpWrapperConfig
)