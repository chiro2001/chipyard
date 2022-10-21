package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.{Config, Parameters}
import chisel3._
import chisel3.experimental.{Analog, BaseModule, DataMirror, Direction, IntParam, attach}
import freechips.rocketchip.util.HeterogeneousBag
import freechips.rocketchip.tilelink.TLBundle
import sifive.blocks.devices.uart.{HasPeripheryUARTModuleImp, UARTPortIO}
import sifive.blocks.devices.spi.{HasPeripherySPI, SPIPortIO}
import chipyard.{CanHaveMasterTLMemPort, HasHarnessSignalReferences}
import chipyard.harness.OverrideHarnessBinder
import chisel3.util.experimental.{AnalogUtils, BoringUtils}
import chisel3.util.{DecoupledIO, HasBlackBoxResource, IrrevocableIO, Queue}
import freechips.rocketchip.amba.axi4.{AXI4Bundle, AXI4BundleParameters}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp, SimpleLazyModule}
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
    th match {
      case pgl22gth: PGL22GTestHarnessImp => {
        require(ports.size == 1)

        val bundles = pgl22gth.pgl22gOuter.ddrClient.out.map(_._1)
        val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
        bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
        ddrClientBundle <> ports.head
      }
    }
  }
})

class WithAXI4DDRMem extends OverrideHarnessBinder({
  (system: CanHaveMasterAXI4MemPort, th: BaseModule with HasHarnessSignalReferences, ports: Seq[HeterogeneousBag[TLBundle]]) => {
    th match {
      case pgl22gth: PGL22GTestHarnessImp => {
        require(ports.size == 1)

        val bundles = pgl22gth.pgl22gOuter.ddrClient.out.map(_._1)
        val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
        bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
        ddrClientBundle <> ports.head
      }
    }
  }
})

class WithPGL22GMemPort extends Config((site, here, up) => {
  case ExtMem => Some(MemoryPortParams(MasterPortParams(
    base = BigInt(0x80000000L),
    size = BigInt(0x10000000),
    beatBytes = site(MemoryBusKey).beatBytes,
    idBits = 4), 1))
})

// class DDR3Mem(memSize: BigInt, params: AXI4BundleParameters)(implicit p: Parameters) extends LazyModule {
class DDR3Mem(memSize: BigInt, params: AXI4BundleParameters) extends RawModule {
  // lazy val module = new LazyModuleImp(this) {
  val axi = IO(Flipped(new AXI4Bundle(params)))
  // val extra = IO(new PGL22GMIGIOClocksResetBundle)
  require(params.dataBits == 128, s"Only support 128bit width data! now is ${params.dataBits}")
  val memInst = Module(new ddr3_core(memSize)).suggestName("ddr3_core_inst")
  val mio = memInst.io
  mio.csysreq_ddrc := false.B
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
      AnalogUtils.add(data.asInstanceOf[Analog], name)
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
  // DataMirror.modulePorts(memInst).foreach(port => {
  //   // println(port)
  //   val (name, data) = port
  //   if (name.startsWith("pad")) {
  //     if (name.contains("in") || name.contains("_dq")) {
  //       addSink(data, name)
  //     } else {
  //       addSource(data, name)
  //     }
  //   } else {
  //     if (inputPortNames.contains(name)) {
  //       addSink(data, name)
  //     } else if (outputPortNames.contains(name)) {
  //       addSource(data, name)
  //     } else {
  //       println(s"ignorePort($name)")
  //     }
  //   }
  // })

  AnalogUtils.add(mio.pad_dq_ch0, "pad_dq_ch0")
  AnalogUtils.add(mio.pad_dqsn_ch0, "pad_dqsn_ch0")
  AnalogUtils.add(mio.pad_dqs_ch0, "pad_dqs_ch0")

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
  axi.w.bits.last <> mio.wlast_0
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
  // }
  val pad_dq_ch0 = IO(Analog(16.W))
  val pad_dqsn_ch0 = IO(Analog(2.W))
  val pad_dqs_ch0 = IO(Analog(2.W))
  attach(mio.pad_dq_ch0, pad_dq_ch0)
  attach(mio.pad_dqsn_ch0, pad_dqsn_ch0)
  attach(mio.pad_dqs_ch0, pad_dqs_ch0)
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
    implicit val p: Parameters = chipyard.iobinders.GetSystemParameters(system)
    require(ports.size == 1)
    th match {
      case pgl22gth: PGL22GBareTestHarnessImp => {
        val (port, edge) = (ports.head, system.memAXI4Node.edges.in.head)
        val clockFreq = p(MemoryBusKey).dtsFrequency.get
        val memSize = p(ExtMem).get.master.size
        // Must input 50Mhz
        // require(clockFreq == BigInt(50000000L), s"MBUS Freq must be 50M, now is $clockFreq")
        // val mem = Module(new DDR3Mem(edge.bundle)).suggestName("ddr3_core_inst")
        withClockAndReset(port.clock, port.reset) {
          // val memInst = LazyModule(new DDR3Mem(memSize, edge.bundle))
          // val mem = Module(memInst.module)
          val mem = Module(new DDR3Mem(memSize, edge.bundle))
          mem.axi <> port.bits

          // attach(pgl22gth.ddrIO.pad_dq_ch0, mem.mio.pad_dq_ch0)
          // attach(pgl22gth.ddrIO.pad_dqsn_ch0, mem.mio.pad_dqsn_ch0)
          // attach(pgl22gth.ddrIO.pad_dqs_ch0, mem.mio.pad_dqs_ch0)
          // val ddrWires = Wire(pgl22gth.ddrIO.cloneType)
          val ddrWires = pgl22gth.ddrIO
          // pgl22gth.ddrIO <> mem.mio
          // pgl22gth.ddrIO <> ddrWires
          def connectWires(a: Data, b: Data, name: String) = {
            BoringUtils.addSource(a, name)
            BoringUtils.addSink(b, name)
          }
          connectWires(mem.mio.pad_addr_ch0, ddrWires.pad_addr_ch0, "pad_addr_ch0")
          connectWires(mem.mio.pad_ba_ch0, ddrWires.pad_ba_ch0, "pad_ba_ch0")
          connectWires(mem.mio.pad_rasn_ch0, ddrWires.pad_rasn_ch0, "pad_rasn_ch0")
          connectWires(mem.mio.pad_casn_ch0, ddrWires.pad_casn_ch0, "pad_casn_ch0")
          connectWires(mem.mio.pad_wen_ch0, ddrWires.pad_wen_ch0, "pad_wen_ch0")
          connectWires(mem.mio.pad_rstn_ch0, ddrWires.pad_rstn_ch0, "pad_rstn_ch0")
          connectWires(mem.mio.pad_ddr_clk_w, ddrWires.pad_ddr_clk_w, "pad_ddr_clk_w")
          connectWires(mem.mio.pad_ddr_clkn_w, ddrWires.pad_ddr_clkn_w, "pad_ddr_clkn_w")
          connectWires(mem.mio.pad_cke_ch0, ddrWires.pad_cke_ch0, "pad_cke_ch0")
          connectWires(mem.mio.pad_csn_ch0, ddrWires.pad_csn_ch0, "pad_csn_ch0")
          connectWires(mem.mio.pad_dm_rdqs_ch0, ddrWires.pad_dm_rdqs_ch0, "pad_dm_rdqs_ch0")
          connectWires(mem.mio.pad_odt_ch0, ddrWires.pad_odt_ch0, "pad_odt_ch0")
          connectWires(ddrWires.pad_loop_in, mem.mio.pad_loop_in, "pad_loop_in")
          connectWires(ddrWires.pad_loop_in_h, mem.mio.pad_loop_in_h, "pad_loop_in_h")
          connectWires(mem.mio.pad_loop_out, ddrWires.pad_loop_out, "pad_loop_out")
          connectWires(mem.mio.pad_loop_out_h, ddrWires.pad_loop_out_h, "pad_loop_out_h")

          // def farAttach(a: Analog, b: Analog, name: String) = {
          //   AnalogUtils.add(a, name)
          //   AnalogUtils.add(b, name)
          // }
          // farAttach(ddrWires.pad_dq_ch0, mem.mio.pad_dq_ch0, "pad_dq_ch0")
          // farAttach(ddrWires.pad_dqsn_ch0, mem.mio.pad_dqsn_ch0, "pad_dqsn_ch0")
          // farAttach(ddrWires.pad_dqs_ch0, mem.mio.pad_dqs_ch0, "pad_dqs_ch0")
          AnalogUtils.add(ddrWires.pad_dq_ch0, "pad_dq_ch0")
          AnalogUtils.add(ddrWires.pad_dqsn_ch0, "pad_dqsn_ch0")
          AnalogUtils.add(ddrWires.pad_dqs_ch0, "pad_dqs_ch0")

          attach(ddrWires.pad_dq_ch0, mem.pad_dq_ch0)
          attach(ddrWires.pad_dqsn_ch0, mem.pad_dqsn_ch0)
          attach(ddrWires.pad_dqs_ch0, mem.pad_dqs_ch0)
          
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
  }
})