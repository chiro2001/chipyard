package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.{Config, Parameters}
import chisel3._
import chisel3.experimental.{BaseModule, DataMirror, Direction, IntParam}
import freechips.rocketchip.util.HeterogeneousBag
import freechips.rocketchip.tilelink.TLBundle
import sifive.blocks.devices.uart.{HasPeripheryUARTModuleImp, UARTPortIO}
import sifive.blocks.devices.spi.{HasPeripherySPI, SPIPortIO}
import chipyard.{CanHaveMasterTLMemPort, HasHarnessSignalReferences}
import chipyard.harness.OverrideHarnessBinder
import chisel3.util.{DecoupledIO, HasBlackBoxResource, IrrevocableIO, Queue}
import freechips.rocketchip.amba.axi4.{AXI4Bundle, AXI4BundleParameters}
import freechips.rocketchip.subsystem.{CacheBlockBytes, CanHaveMasterAXI4MemPort, ExtMem, MasterPortParams, MemoryBusKey, MemoryPortParams}
import sifive.fpgashells.ip.pango.ddr3.{PGL22GMIGIOClocksReset, PGL22GMIGIOClocksResetBundle, ddr3_core}
import testchipip.{ClockedAndResetIO, SimDRAM}

/** * UART ** */
class WithUART extends OverrideHarnessBinder({
  (system: HasPeripheryUARTModuleImp, th: BaseModule with HasHarnessSignalReferences, ports: Seq[UARTPortIO]) => {
    th match {
      case pgl22gth: PGL22GFPGATestHarnessImp => {
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
    th match { case pgl22gth: PGL22GFPGATestHarnessImp => {
      require(ports.size == 1)

      val bundles = pgl22gth.pgl22gOuter.ddrClient.out.map(_._1)
      val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
      bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
      ddrClientBundle <> ports.head
    } }
  }
})

// class WithAXI4DDRMem extends OverrideHarnessBinder({
//   (system: CanHaveMasterAXI4MemPort, th: BaseModule with HasHarnessSignalReferences, ports: Seq[HeterogeneousBag[TLBundle]]) => {
//     th match { case pgl22gth: PGL22GFPGATestHarnessImp => {
//       require(ports.size == 1)
//
//       val bundles = pgl22gth.pgl22gOuter.ddrClient.out.map(_._1)
//       val ddrClientBundle = Wire(new HeterogeneousBag(bundles.map(_.cloneType)))
//       bundles.zip(ddrClientBundle).foreach { case (bundle, io) => bundle <> io }
//       ddrClientBundle <> ports.head
//     } }
//   }
// })

class WithPGL22GMemPort extends Config((site, here, up) => {
  case ExtMem => Some(MemoryPortParams(MasterPortParams(
    base = BigInt(0x80000000L),
    size = BigInt(0x10000000),
    beatBytes = site(MemoryBusKey).beatBytes,
    idBits = 4), 1))
})

class DDR3Mem(memSize: BigInt, params: AXI4BundleParameters) extends Module {
  val axi = IO(Flipped(new AXI4Bundle(params)))
  val extra = IO(new PGL22GMIGIOClocksResetBundle)
  require(params.dataBits == 128, s"Only support 128bit width data! now is ${params.dataBits}")
  val memInst = Module(new ddr3_core(memSize)).suggestName("ddr3_core_inst")
  val mio = memInst.io
  mio <> extra
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
  axi.r.ready <> mio.rready_0
  axi.r.bits.id <> mio.rid_0
  axi.r.bits.data <> mio.rdata_0
  axi.r.bits.resp <> mio.rresp_0
  axi.r.bits.last <> mio.rlast_0
  axi.r.valid <> mio.rvalid_0
}

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
        // mem.clock := port.clock
        // mem.reset := port.reset

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