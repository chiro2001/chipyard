package pgl22g.simple

import chipsalliance.rocketchip.config.{Config, Field, Parameters}
import chisel3._
import freechips.rocketchip.amba.axi4.{AXI4Fragmenter, AXI4MasterNode, AXI4MasterParameters, AXI4MasterPortParameters, AXI4RAM, AXI4SlaveNode, AXI4SlaveParameters, AXI4SlavePortParameters, AXI4ToTL, AXI4UserYanker, AXI4Xbar}
import freechips.rocketchip.diplomacy.{AddressSet, LazyModule, LazyModuleImp, LazyModuleImpLike, LazyRawModuleImp, TransferSizes}
import freechips.rocketchip.tilelink.{TLBuffer, TLFIFOFixer, TLIdentityNode, TLWidthWidget}
import pgl22g.DDR3Mem
import sifive.blocks.devices.uart.{UART, UARTParams}
import sifive.fpgashells.ip.pango.ddr3.PGL22GMIGIODDR
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

  val axiMasters = (0 until cpuCount).flatMap(i =>
    Seq(
      AXI4MasterNode(Seq(AXI4MasterPortParameters(
        masters = Seq(AXI4MasterParameters("cpuIBus" + i))))),
      AXI4MasterNode(Seq(AXI4MasterPortParameters(
        masters = Seq(AXI4MasterParameters("cpuDBus" + i)))))
    ))
  val xbar = AXI4Xbar()
  val axiDeviceMem = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address = Seq(AddressSet(0x80000000L, 0xfffffffL)),
      executable = true,
      supportsWrite = TransferSizes(1, 256 * 8),
      supportsRead = TransferSizes(1, 256 * 8))),
    beatBytes = 8)))
  axiDeviceMem := xbar
  val rom = AXI4RAM(
    AddressSet(0x10000L, 0xffffL),
    beatBytes = 8
  )
  rom := AXI4Fragmenter() := xbar
  axiMasters.foreach(master => xbar := master)

  // val slaveNode = TLIdentityNode()
  // val masterNode = TLIdentityNode()
  // // xbar := AXI4Fragmenter() := AXI4UserYanker(Some(2)) := AXI4ToTL() := TLWidthWidget(4) := slaveNode
  // (masterNode
  //   := TLBuffer()
  //   := TLFIFOFixer(TLFIFOFixer.all)
  //   := TLWidthWidget(4)
  //   := AXI4ToTL()
  //   // := AXI4UserYanker(Some(2))
  //   := AXI4Fragmenter()
  //   := xbar)
  // slaveNode := masterNode
  //
  // val uart = LazyModule(new UART(4, UARTParams(0x54000000L)))
  // slaveNode := uart.node

  override def module = new VexChipSmpWrapperImpl(this)
}

class VexChipSmpWrapperImpl(_outer: VexChipSmpWrapper) extends LazyRawModuleImp(_outer) {

  import _outer.config._

  val sys_clock = IO(Input(Clock()))
  val reset = IO(Input(Bool()))

  val DDR3Mem = Module(new DDR3Mem(0x80000000L, _outer.axiDeviceMem.in.head._1.params))
  val ddr = IO(new PGL22GMIGIODDR)
  ddr.connectPads(DDR3Mem.ddrIO)
  // mem.ddrIO.pad_loop_in := ddr.pad_loop_in
  // mem.ddrIO.pad_loop_in_h := ddr.pad_loop_in_h
  val mainClock = DDR3Mem.ddrCtrl.pll_aclk_1
  val mainReset = WireInit(!DDR3Mem.ddrCtrl.pll_lock)
  DDR3Mem.ddrCtrl.aclk_1.get := mainClock
  DDR3Mem.axi <> _outer.axiDeviceMem.in.head._1
  DDR3Mem.ddrCtrl.csysreq_ddrc := true.B
  DDR3Mem.ddrCtrl.csysreq_1 := true.B
  DDR3Mem.ddrCtrl.ddrc_rst := false.B
  DDR3Mem.ddrCtrl.top_rst_n := reset
  DDR3Mem.ddrCtrl.pll_refclk_in := sys_clock

  withClockAndReset(mainClock, mainReset) {
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
      cpu.io.clk := mainClock
      cpu.io.reset := mainReset
    })
    cpus.zip(_outer.axiMasters.grouped(2).toSeq).foreach(x => {
      x._1.io.connectIMem(x._2.head.out.head._1)
      x._1.io.connectDMem(x._2(1).out.head._1)
    })

    def generateMemWatcher(address: Long, name: String = "resultReg"): UInt = {
      val resultReg = RegInit(0.U(64.W)).suggestName(name)
      val resultAddress = address.U
      val validToResult = DDR3Mem.axi.aw.bits.addr === resultAddress && DDR3Mem.axi.aw.ready && DDR3Mem.axi.aw.valid
      val readyToResult = RegInit(false.B)
      val writtenResult = RegInit(false.B)
      val writingResult = (validToResult || readyToResult) && DDR3Mem.axi.w.valid && DDR3Mem.axi.w.ready &&
        DDR3Mem.axi.w.bits.last && writtenResult
      when(validToResult) {
        readyToResult := true.B
      }
      when(writingResult) {
        writtenResult := true.B
        resultReg := DDR3Mem.axi.w.bits.data
      }
      resultReg
    }

    val scores = IO(Output(UInt(64.W))).suggestName("scores")
    scores := generateMemWatcher(0x85000000L, "scoresReg")
  }
}

case object VexChipSmpKey extends Field[VexChipSmpWrapperConfig]

class WithVexChipSmpWrapperConfig extends Config((site, here, up) => {
  case VexChipSmpKey => VexChipSmpWrapperConfig.default
})

class VexChipSmpConfig extends Config(
  new WithVexChipSmpWrapperConfig
)