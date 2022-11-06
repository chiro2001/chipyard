package pgl22g

import Chisel.Data
import chipsalliance.rocketchip.config.Parameters
import chipyard.harness.OverrideHarnessBinder
import chipyard.iobinders.JTAGChipIO
import chipyard.{CanHaveMasterTLMemPort, HasHarnessSignalReferences}
import chisel3._
import chisel3.experimental.{BaseModule, DataMirror, Direction, attach}
import chisel3.util.{DecoupledIO, IrrevocableIO, Queue}
import freechips.rocketchip.amba.axi4.{AXI4Bundle, AXI4BundleParameters}
import freechips.rocketchip.devices.debug.HasPeripheryDebug
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink.TLBundle
import freechips.rocketchip.util.HeterogeneousBag
import pgl22g.testharness._
import sifive.blocks.devices.spi.HasPeripherySPIFlashModuleImp
import sifive.blocks.devices.uart.{HasPeripheryUARTModuleImp, UARTPortIO}
import sifive.fpgashells.ip.pango.ddr3.{PGL22GMIGIOClocksResetBundle, PGL22GMIGIODDR, PGL22GMIGIODDRIO, PGL22GMIGIODDRIO64, ddr3_core, ddr3_core_64}
import testchipip.{ClockedAndResetIO, SPIChipIO}
import vexriscv.chipyard.{HasCoreInternalDebug, VexJTAGChipIO}


/** * UART ** */
class WithUARTHarnessBinder extends OverrideHarnessBinder({
  (system: HasPeripheryUARTModuleImp, th: BaseModule with HasHarnessSignalReferences, ports: Seq[UARTPortIO]) => {
    th match {
      case th: PGL22GTestHarnessUartImp => {
        th.uart <> ports.head
      }
      case th: PGL22GTestHarnessPerfUartImp => {
        withClockAndReset(th.sys_clock, th.buildtopReset) {
          th.uart <> ports.head
        }
      }
      case th: PGL22GTestHarnessUartTopClockImp => {
        withClockAndReset(th.buildtopClock, th.buildtopReset) {
          th.uart <> ports.head
        }
      }
      case th: PGL22GSimTestHarnessImpl => {
      }
    }
  }
})

class WithJTAGHarnessBinder extends OverrideHarnessBinder({
  (system: HasPeripheryDebug, th: BaseModule with HasHarnessSignalReferences, ports: Seq[Data]) => {
    th match {
      case th: PGL22GTestHarnessJtagImpl => {
        println(s"WithJTAGHarnessBinder - PGL22GTestHarnessJtagImpl! ports: ${ports}")
        ports.map {
          case j: JTAGChipIO =>
            th.jtag match {
              case Some(jtag) =>
                j.TCK <> jtag.TCK
                j.TMS <> jtag.TMS
                j.TDI <> jtag.TDI
                j.TDO <> jtag.TDO
            }
        }
      }
      case th: PGL22GSimTestHarnessImpl => {
      }
    }
  }
})

class WithInternalJTAGHarnessBinder extends OverrideHarnessBinder({
  (system: HasCoreInternalDebug, th: BaseModule with HasHarnessSignalReferences, ports: Seq[Data]) => {
    th match {
      case th: PGL22GTestHarnessJtagImpl =>
        println(s"WithInternalJTAGHarnessBinder - PGL22GTestHarnessJtagImpl! ports: ${ports}")
        ports.map {
          case j: JTAGChipIO =>
            system.jtagBundle.map {
              t: VexJTAGChipIO =>
                j.TCK <> t.jtag_tck
                j.TMS <> t.jtag_tms
                j.TDI <> t.jtag_tdi
                j.TDO <> t.jtag_tdo
            }
        }
      case th: PGL22GSimTestHarnessImpl => {
      }
    }
  }
})

class WithSPIFlashHarnessBinder extends OverrideHarnessBinder({
  (system: HasPeripherySPIFlashModuleImp, th: BaseModule with HasHarnessSignalReferences, ports: Seq[SPIChipIO]) => {
    th match {
      case th: PGL22GTestHarnessSPIFlashImpl => {
        require(ports.size == 1)
        val s = ports.head
        th.qspi.sck := s.sck
        require(s.cs.size == 1)
        th.qspi.cs := s.cs.head
        require(s.dq.size == 4)
        s.dq.zip(th.qspi.dq).foreach(x => attach(x._1, x._2))
      }
      case th: PGL22GSimTestHarnessImpl => {
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

class DDR3Mem(memSize: BigInt, params: AXI4BundleParameters) extends RawModule {
  println(s"DDR3 Interface WIDTH=${params.dataBits}")
  val axi = IO(Flipped(new AXI4Bundle(params)))
  require(params.dataBits == 128 || params.dataBits == 64, s"Only support 128 or 64bit width data! now is ${params.dataBits}")
  val memInst = Module(if (params.dataBits == 128) new ddr3_core(memSize) else new ddr3_core_64(memSize)).suggestName("ddr3_core_inst")
  val ddrIO = IO(new PGL22GMIGIODDR)
  val mio = memInst.io
  mio.csysreq_ddrc := true.B
  val ddrCtrl = IO(new PGL22GMIGIOClocksResetBundle)

  mio match {
    case mio: PGL22GMIGIODDRIO => {
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
    }
    case mio: PGL22GMIGIODDRIO64 => {
      axi.aw.bits.id <> mio.awid_1
      axi.aw.bits.addr <> mio.awaddr_1
      axi.aw.bits.len <> mio.awlen_1
      axi.aw.bits.size <> mio.awsize_1
      axi.aw.bits.burst <> mio.awburst_1
      axi.aw.bits.lock <> mio.awlock_1
      axi.aw.valid <> mio.awvalid_1
      axi.aw.ready <> mio.awready_1
      axi.w.bits.data <> mio.wdata_1
      axi.w.bits.strb <> mio.wstrb_1
      axi.w.bits.last <> mio.wlast_1
      axi.w.valid <> mio.wvalid_1
      axi.w.ready <> mio.wready_1
      axi.b.ready <> mio.bready_1
      axi.b.bits.id <> mio.bid_1
      axi.b.bits.resp <> mio.bresp_1
      axi.b.valid <> mio.bvalid_1
      axi.ar.bits.id <> mio.arid_1
      axi.ar.bits.addr <> mio.araddr_1
      axi.ar.bits.len <> mio.arlen_1
      axi.ar.bits.size <> mio.arsize_1
      axi.ar.bits.burst <> mio.arburst_1
      axi.ar.bits.lock <> mio.arlock_1
      axi.ar.valid <> mio.arvalid_1
      axi.ar.ready <> mio.arready_1
      axi.r.ready <> mio.rready_1
      axi.r.bits.id <> mio.rid_1
      axi.r.bits.data <> mio.rdata_1
      axi.r.bits.resp <> mio.rresp_1
      axi.r.bits.last <> mio.rlast_1
      axi.r.valid <> mio.rvalid_1
    }
  }

  attach(mio.pad_dq_ch0, ddrIO.pad_dq_ch0)
  attach(mio.pad_dqsn_ch0, ddrIO.pad_dqsn_ch0)
  attach(mio.pad_dqs_ch0, ddrIO.pad_dqs_ch0)
  mio.pad_addr_ch0 <> ddrIO.pad_addr_ch0
  mio.pad_ba_ch0 <> ddrIO.pad_ba_ch0
  mio.pad_rasn_ch0 <> ddrIO.pad_rasn_ch0
  mio.pad_casn_ch0 <> ddrIO.pad_casn_ch0
  mio.pad_wen_ch0 <> ddrIO.pad_wen_ch0
  mio.pad_rstn_ch0 <> ddrIO.pad_rstn_ch0
  mio.pad_ddr_clk_w <> ddrIO.pad_ddr_clk_w
  mio.pad_ddr_clkn_w <> ddrIO.pad_ddr_clkn_w
  mio.pad_cke_ch0 <> ddrIO.pad_cke_ch0
  mio.pad_csn_ch0 <> ddrIO.pad_csn_ch0
  mio.pad_dm_rdqs_ch0 <> ddrIO.pad_dm_rdqs_ch0
  mio.pad_odt_ch0 <> ddrIO.pad_odt_ch0
  mio.pad_loop_in <> ddrIO.pad_loop_in
  mio.pad_loop_in_h <> ddrIO.pad_loop_in_h
  mio.pad_loop_out <> ddrIO.pad_loop_out
  mio.pad_loop_out_h <> ddrIO.pad_loop_out_h

  mio.pll_refclk_in <> ddrCtrl.pll_refclk_in
  mio.top_rst_n <> ddrCtrl.top_rst_n
  mio.ddrc_rst <> ddrCtrl.ddrc_rst
  mio.pll_lock <> ddrCtrl.pll_lock
  mio.ddrphy_rst_done <> ddrCtrl.ddrphy_rst_done
  mio.ddrc_init_done <> ddrCtrl.ddrc_init_done
  mio.pll_aclk_0 <> ddrCtrl.pll_aclk_0
  mio.pll_aclk_1 <> ddrCtrl.pll_aclk_1
  mio.pll_aclk_2 <> ddrCtrl.pll_aclk_2
  // mio.pll_pclk <> ddrCtrl.pll_pclk
  mio.csysreq_ddrc <> ddrCtrl.csysreq_ddrc
  mio.csysack_ddrc <> ddrCtrl.csysack_ddrc
  mio.cactive_ddrc <> ddrCtrl.cactive_ddrc
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

class WithBlackBoxDDRMem(additionalLatency: Int = 0, width: Int = 128) extends OverrideHarnessBinder({
  (system: CanHaveMasterAXI4MemPort, th: HasHarnessSignalReferences, ports: Seq[ClockedAndResetIO[AXI4Bundle]]) => {
    implicit val p: Parameters = chipyard.iobinders.GetSystemParameters(system)
    require(ports.size == 1)
    th match {
      case pgl22gth: PGL22GTestHarnessDDRImp => {
        val (port, edge) = (ports.head, system.memAXI4Node.edges.in.head)
        val clockFreq = p(MemoryBusKey).dtsFrequency.get
        val memSize = p(ExtMem).get.master.size
        // Must input 50Mhz
        // require(clockFreq == BigInt(50000000L), s"MBUS Freq must be 50M, now is $clockFreq")
        withClockAndReset(port.clock, port.reset) {
          val mem = Module(new DDR3Mem(memSize, edge.bundle))
          mem.axi <> port.bits
          val ddr = pgl22gth.ddr
          mem.ddrIO.pad_addr_ch0 <> ddr.pad_addr_ch0
          mem.ddrIO.pad_ba_ch0 <> ddr.pad_ba_ch0
          mem.ddrIO.pad_rasn_ch0 <> ddr.pad_rasn_ch0
          mem.ddrIO.pad_casn_ch0 <> ddr.pad_casn_ch0
          mem.ddrIO.pad_wen_ch0 <> ddr.pad_wen_ch0
          mem.ddrIO.pad_rstn_ch0 <> ddr.pad_rstn_ch0
          mem.ddrIO.pad_ddr_clk_w <> ddr.pad_ddr_clk_w
          mem.ddrIO.pad_ddr_clkn_w <> ddr.pad_ddr_clkn_w
          mem.ddrIO.pad_cke_ch0 <> ddr.pad_cke_ch0
          mem.ddrIO.pad_csn_ch0 <> ddr.pad_csn_ch0
          mem.ddrIO.pad_dm_rdqs_ch0 <> ddr.pad_dm_rdqs_ch0
          mem.ddrIO.pad_odt_ch0 <> ddr.pad_odt_ch0
          ddr.pad_loop_in <> mem.ddrIO.pad_loop_in
          ddr.pad_loop_in_h <> mem.ddrIO.pad_loop_in_h
          mem.ddrIO.pad_loop_out <> ddr.pad_loop_out
          mem.ddrIO.pad_loop_out_h <> ddr.pad_loop_out_h
          attach(ddr.pad_dq_ch0, mem.ddrIO.pad_dq_ch0)
          attach(ddr.pad_dqsn_ch0, mem.ddrIO.pad_dqsn_ch0)
          attach(ddr.pad_dqs_ch0, mem.ddrIO.pad_dqs_ch0)

          pgl22gth.pll_lock := mem.ddrCtrl.pll_lock
          pgl22gth.ddrphy_rst_done := mem.ddrCtrl.ddrphy_rst_done
          pgl22gth.ddrc_init_done := mem.ddrCtrl.ddrc_init_done
          pgl22gth.pll_clk_bus := (if (width == 64) mem.ddrCtrl.pll_aclk_1 else mem.ddrCtrl.pll_aclk_0)
          mem.ddrCtrl.pll_refclk_in := pgl22gth.sysclk
          mem.ddrCtrl.top_rst_n := pgl22gth.hardResetN
          // mem.ddrCtrl.ddrc_rst := pgl22gth.hardResetN
          mem.ddrCtrl.ddrc_rst := false.B
          // mem.ddrCtrl.csysreq_ddrc := false.B
          mem.ddrCtrl.csysreq_ddrc := true.B

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