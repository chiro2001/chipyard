package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.{Config, Parameters}
import chisel3._
import chisel3.experimental.{Analog, BaseModule, DataMirror, Direction, IntParam}
import freechips.rocketchip.util.HeterogeneousBag
import freechips.rocketchip.tilelink.TLBundle
import sifive.blocks.devices.uart.{HasPeripheryUARTModuleImp, UARTPortIO}
import sifive.blocks.devices.spi.{HasPeripherySPI, SPIPortIO}
import chipyard.{CanHaveMasterTLMemPort, HasHarnessSignalReferences}
import chipyard.harness.OverrideHarnessBinder
import chisel3.util.experimental.{AnalogUtils, BoringUtils}
import chisel3.util.{DecoupledIO, HasBlackBoxResource, IrrevocableIO, Queue}
import freechips.rocketchip.amba.axi4.{AXI4Bundle, AXI4BundleParameters}
import freechips.rocketchip.subsystem.{CacheBlockBytes, CanHaveMasterAXI4MemPort, ExtMem, MasterPortParams, MemoryBusKey, MemoryPortParams}
import sifive.fpgashells.ip.pango.ddr3.{PGL22GMIGIOClocksReset, PGL22GMIGIOClocksResetBundle, ddr3_core}
import testchipip.{ClockedAndResetIO, SimDRAM}
// import chipyard.fpga.pgl22g.{PGL22GTestHarnessImp => UseTestHarnessImp}
import chipyard.fpga.pgl22g.{PGL22GBareTestHarnessImp => UseTestHarnessImp}


/** * UART ** */
class WithUART extends OverrideHarnessBinder({
  (system: HasPeripheryUARTModuleImp, th: BaseModule with HasHarnessSignalReferences, ports: Seq[UARTPortIO]) => {
    th match {
      case pgl22gth: UseTestHarnessImp => {
        pgl22gth.pgl22gOuter.io_uart_bb.bundle <> ports.head
      }
    }
  }
})
//
// /*** SPI ***/
// class WithSPISDCard extends OverrideHarnessBinder({
//   (system: HasPeripherySPI, th: BaseModule with HasHarnessSignalReferences, ports: Seq[SPIPortIO]) => {
//     th match { case pgl22gth: PGL22GFPGATestHarnessImp => {
//       pgl22gth.pgl22gOuter.io_spi_bb.bundle <> ports.head
//     } }
//   }
// })

/** * Experimental DDR ** */
class WithDDRMem extends OverrideHarnessBinder({
  (system: CanHaveMasterTLMemPort, th: BaseModule with HasHarnessSignalReferences, ports: Seq[HeterogeneousBag[TLBundle]]) => {
    th match { case pgl22gth: PGL22GTestHarnessImp => {
      require(ports.size == 1)

      val bundles = pgl22gth.pgl22gOuter.ddrClient.out.map(_._1)
      val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
      bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
      ddrClientBundle <> ports.head
    } }
  }
})

class WithAXI4DDRMem extends OverrideHarnessBinder({
  (system: CanHaveMasterAXI4MemPort, th: BaseModule with HasHarnessSignalReferences, ports: Seq[HeterogeneousBag[TLBundle]]) => {
    th match { case pgl22gth: PGL22GTestHarnessImp => {
      require(ports.size == 1)

      val bundles = pgl22gth.pgl22gOuter.ddrClient.out.map(_._1)
      val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
      bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
      ddrClientBundle <> ports.head
    } }
  }
})

class WithPGL22GMemPort extends Config((site, here, up) => {
  case ExtMem => Some(MemoryPortParams(MasterPortParams(
    base = BigInt(0x80000000L),
    size = BigInt(0x10000000),
    beatBytes = site(MemoryBusKey).beatBytes,
    idBits = 4), 1))
})

class DDR3Mem(memSize: BigInt, params: AXI4BundleParameters) extends RawModule {
  val axi = IO(Flipped(new AXI4Bundle(params)))
  // val extra = IO(new PGL22GMIGIOClocksResetBundle)
  require(params.dataBits == 128, s"Only support 128bit width data! now is ${params.dataBits}")
  val memInst = Module(new ddr3_core(memSize)).suggestName("ddr3_core_inst")
  val mio = memInst.io
  // // mio <> extra
  // // 外部参考时钟输入
  // mio.pll_refclk_in <> extra.pll_refclk_in
  // // 外部复位输入
  // mio.top_rst_n <> extra.top_rst_n
  // // DDRC 的复位输入
  // mio.ddrc_rst <> extra.ddrc_rst
  // // ddr3_core 内部 PLL lock 信号。
  // mio.pll_lock <> extra.pll_lock
  // // DDRPHY 复位完成标志
  // mio.ddrphy_rst_done <> extra.ddrphy_rst_done
  // // DDRC 的初始化完成标志
  // mio.ddrc_init_done <> extra.ddrc_init_done
  // // Axi4 Port0 的时钟
  // mio.pll_aclk_0 <> extra.pll_aclk_0
  // // Axi4 Port1 的时钟
  // mio.pll_aclk_1 <> extra.pll_aclk_1
  // // Axi4 Port2 的时钟
  // mio.pll_aclk_2 <> extra.pll_aclk_2
  // // APB Port 的时钟
  // // mio.pll_pclk <> extra.pll_pclk
  // // DDRC 低功耗请求输入
  // mio.csysreq_ddrc <> extra.csysreq_ddrc
  // // DDRC 低功耗响应
  // mio.csysack_ddrc <> extra.csysack_ddrc
  // // DDRC 激活标志
  // mio.cactive_ddrc <> extra.cactive_ddrc

  // mio.pll_refclk_in := clock
  // mio.top_rst_n := !(reset.asBool)
  // mio.ddrc_rst := !(reset.asBool)
  mio.csysreq_ddrc := false.B

  // // 外部参考时钟输入
  // val pll_refclk_in = Wire(mio.pll_refclk_in.cloneType)
  // // 外部复位输入
  // val top_rst_n = Wire(mio.top_rst_n.cloneType)
  // // DDRC 的复位输入
  // val ddrc_rst = Wire(mio.ddrc_rst.cloneType)
  // // ddr3_core 内部 PLL lock 信号。
  // val pll_lock = Wire(mio.pll_lock.cloneType)
  // // DDRPHY 复位完成标志
  // val ddrphy_rst_done = Wire(mio.ddrphy_rst_done.cloneType)
  // // DDRC 的初始化完成标志
  // val ddrc_init_done = Wire(mio.ddrc_init_done.cloneType)
  // // Axi4 Port0 的时钟
  // val pll_aclk_0 = Wire(mio.pll_aclk_0.cloneType)
  // // Axi4 Port1 的时钟
  // val pll_aclk_1 = Wire(mio.pll_aclk_1.cloneType)
  // // Axi4 Port2 的时钟
  // val pll_aclk_2 = Wire(mio.pll_aclk_2.cloneType)
  // // APB Port 的时钟
  // // val pll_pclk = Wire(mio.pll_pclk.cloneType)
  // // DDRC 低功耗请求输入
  // // val csysreq_ddrc = Wire(mio.csysreq_ddrc.cloneType)
  // // DDRC 低功耗响应
  // val csysack_ddrc = Wire(mio.csysack_ddrc.cloneType)
  // // DDRC 激活标志
  // val cactive_ddrc = Wire(mio.cactive_ddrc.cloneType)
  //
  // // 外部参考时钟输入
  // BoringUtils.addSink(pll_refclk_in, "pll_refclk_in")
  // // 外部复位输入
  // BoringUtils.addSink(top_rst_n, "top_rst_n")
  // // DDRC 的复位输入
  // BoringUtils.addSink(ddrc_rst, "ddrc_rst")
  // // ddr3_core 内部 PLL lock 信号。
  // BoringUtils.addSource(pll_lock, "pll_lock")
  // // DDRPHY 复位完成标志
  // BoringUtils.addSource(ddrphy_rst_done, "ddrphy_rst_done")
  // // DDRC 的初始化完成标志
  // BoringUtils.addSource(ddrc_init_done, "ddrc_init_done")
  // // Axi4 Port0 的时钟
  // BoringUtils.addSource(pll_aclk_0, "pll_aclk_0")
  // // Axi4 Port1 的时钟
  // BoringUtils.addSource(pll_aclk_1, "pll_aclk_1")
  // // Axi4 Port2 的时钟
  // BoringUtils.addSource(pll_aclk_2, "pll_aclk_2")
  // // APB Port 的时钟
  // // BoringUtils.addSource(pll_pclk, "pll_pclk")
  // // DDRC 低功耗请求输入
  // // BoringUtils.addSink(csysreq_ddrc, "csysreq_ddrc")
  // // DDRC 低功耗响应
  // BoringUtils.addSource(csysack_ddrc, "csysack_ddrc")
  // // DDRC 激活标志
  // BoringUtils.addSource(cactive_ddrc, "cactive_ddrc")
  //
  // pll_lock := mio.pll_lock
  // ddrphy_rst_done := mio.ddrphy_rst_done
  // ddrc_init_done := mio.ddrc_init_done
  // pll_aclk_0 := mio.pll_aclk_0
  // pll_aclk_1 := mio.pll_aclk_1
  // pll_aclk_2 := mio.pll_aclk_2
  //
  // mio.pll_refclk_in := pll_refclk_in
  // mio.top_rst_n := top_rst_n
  // mio.ddrc_rst := ddrc_rst
  // // mio.csysreq_ddrc := csysreq_ddrc
  
  var inputPortNames: Seq[String] = Seq()
  var outputPortNames: Seq[String] = Seq()
  inputPortNames = inputPortNames :+ "pll_refclk_in"
  inputPortNames = inputPortNames :+ "top_rst_n"
  inputPortNames = inputPortNames :+ "ddrc_rst"
  outputPortNames = outputPortNames :+ "pll_lock"
  outputPortNames = outputPortNames :+ "ddrphy_rst_done"
  outputPortNames = outputPortNames :+ "ddrc_init_done"
  // outputPortNames = outputPortNames :+ "pll_aclk_0"
  // outputPortNames = outputPortNames :+ "pll_aclk_1"
  outputPortNames = outputPortNames :+ "pll_aclk_2"
  // inputPortNames = inputPortNames :+ "csysreq_ddrc"
  // outputPortNames = outputPortNames :+ "csysack_ddrc"
  // outputPortNames = outputPortNames :+ "cactive_ddrc"
  val addSink = (data: Data, name: String) => {
    if (data.isInstanceOf[Analog]) {
      println(s"addAnalogSink($name)")
      AnalogUtils.addSink(data, name)
    } else {
      println(s"addSink($name)")
      BoringUtils.addSink(data, name)
    }
  }
  val addSource = (data: Data, name: String) => {
    println(s"addSource($name)")
    BoringUtils.addSource(data, name)
  }

  // println(inputPortNames, outputPortNames)
  DataMirror.modulePorts(memInst).foreach(port => {
    // println(port)
    val (name, data) = port
    if (name.startsWith("pad")) {
      if (name.contains("in") || name.contains("_dq")) {
        addSink(data, name)
      } else {
        addSource(data, name)
      }
    } else {
      if (inputPortNames.contains(name)) {
        addSink(data, name)
      } else if (outputPortNames.contains(name)) {
        addSource(data, name)
      } else {
        println(s"ignorePort($name)")
      }
    }
  })

  axi.aw.bits.id <> mio.awid_0
  axi.aw.bits.addr <> mio.awaddr_0
  axi.aw.bits.len <> mio.awlen_0
  axi.aw.bits.size <> mio.awsize_0
  axi.aw.bits.burst <> mio.awburst_0
  axi.aw.bits.lock <> mio.awlock_0
  axi.aw.valid <> mio.awvalid_0
  axi.aw.ready <> mio.awready_0
  axi.w.bits.data <> mio.wdata_0
  axi.w.bits.strb <> mio.wstrb_0
  axi.w.bits.last<> mio.wlast_0
  axi.w.valid <> mio.wvalid_0
  axi.w.ready <> mio.wready_0
  axi.b.ready <> mio.bready_0
  axi.b.bits.id <> mio.bid_0
  axi.b.bits.resp <> mio.bresp_0
  axi.b.valid <> mio.bvalid_0
  axi.ar.bits.id <> mio.arid_0
  axi.ar.bits.addr <> mio.araddr_0
  axi.ar.bits.len <> mio.arlen_0
  axi.ar.bits.size <> mio.arsize_0
  axi.ar.bits.burst <> mio.arburst_0
  axi.ar.bits.lock <> mio.arlock_0
  axi.ar.valid <> mio.arvalid_0
  axi.ar.ready <> mio.arready_0
  axi.r.ready <> mio.rready_0
  axi.r.bits.id <> mio.rid_0
  axi.r.bits.data <> mio.rdata_0
  axi.r.bits.resp <> mio.rresp_0
  axi.r.bits.last <> mio.rlast_0
  axi.r.valid <> mio.rvalid_0
}

// object DDR3Mem {
//   def connectMem(dut: CanHaveMasterAXI4MemPort)(implicit p: Parameters): Seq[DDR3Mem] = {
//     dut.mem_axi4.zip(dut.memAXI4Node.in).map { case (io, (_, edge)) =>
//       val memSize = p(ExtMem).get.master.size
//       // val mem = LazyModule(new DDR3Mem(memSize, edge.bundle))
//       val mem = Module(new DDR3Mem(memSize, edge.bundle))
//       mem.axi <> io
//       mem
//     }
//   }
// }

class WithBlackBoxDDRMem(additionalLatency: Int = 0) extends OverrideHarnessBinder({
  (system: CanHaveMasterAXI4MemPort, th: HasHarnessSignalReferences, ports: Seq[ClockedAndResetIO[AXI4Bundle]]) => {
    val p: Parameters = chipyard.iobinders.GetSystemParameters(system)
    (ports zip system.memAXI4Node.edges.in).map { case (port, edge) =>
      val clockFreq = p(MemoryBusKey).dtsFrequency.get
      val memSize = p(ExtMem).get.master.size
      // Must input 50Mhz
      // require(clockFreq == BigInt(50000000L), s"MBUS Freq must be 50M, now is $clockFreq")
      // val mem = Module(new DDR3Mem(edge.bundle)).suggestName("ddr3_core_inst")
      withClockAndReset(port.clock, port.reset) {
        val mem = Module(new DDR3Mem(memSize, edge.bundle))
        mem.axi <> port.bits
        // mem.extra.pll_refclk_in := port.clock
        // mem.extra.top_rst_n := !(port.reset.asBool)
        // mem.extra.ddrc_rst := !(port.reset.asBool)
        // mem.extra.csysreq_ddrc := false.B

        // Bug in Chisel implementation. See https://github.com/chipsalliance/chisel3/pull/1781
        def Decoupled[T <: Data](irr: IrrevocableIO[T]): DecoupledIO[T] = {
          require(DataMirror.directionOf(irr.bits) == Direction.Output, "Only safe to cast produced Irrevocable bits to Decoupled.")
          val d = Wire(new DecoupledIO(chiselTypeOf(irr.bits)))
          d.bits := irr.bits
          d.valid := irr.valid
          irr.ready := d.ready
          d
        }

        if (additionalLatency > 0) {
          withClockAndReset(port.clock, port.reset) {
            mem.axi.aw <> (0 until additionalLatency).foldLeft(Decoupled(port.bits.aw))((t, _) => Queue(t, 1, pipe = true))
            mem.axi.w <> (0 until additionalLatency).foldLeft(Decoupled(port.bits.w))((t, _) => Queue(t, 1, pipe = true))
            port.bits.b <> (0 until additionalLatency).foldLeft(Decoupled(mem.axi.b))((t, _) => Queue(t, 1, pipe = true))
            mem.axi.ar <> (0 until additionalLatency).foldLeft(Decoupled(port.bits.ar))((t, _) => Queue(t, 1, pipe = true))
            port.bits.r <> (0 until additionalLatency).foldLeft(Decoupled(mem.axi.r))((t, _) => Queue(t, 1, pipe = true))
          }
        }
      }
    }
  }
})