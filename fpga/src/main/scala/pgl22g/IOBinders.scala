package pgl22g

import barstools.iocell.chisel.IOCell
import chipyard.CanHaveMasterTLMemPort
import chisel3._
import chisel3.experimental.{DataMirror, IO, noPrefix}
import freechips.rocketchip.util._
import freechips.rocketchip.devices.debug._
import chipyard.iobinders.{ComposeIOBinder, GetSystemParameters, IOCellKey, OverrideIOBinder, OverrideLazyIOBinder}
import freechips.rocketchip.amba.axi4.AXI4Bundle
import freechips.rocketchip.diplomacy.InModuleBody
import freechips.rocketchip.subsystem.{BaseSubsystem, CanHaveMasterAXI4MemPort}
import freechips.rocketchip.tilelink.TLBundle
import sifive.blocks.devices.uart.HasPeripheryUARTModuleImp
import pgl22g._
import vexriscv.chipyard.{HasCoreInternalDebugModuleImp, VexJTAGChipIO}

class WithDebugResetPassthrough extends ComposeIOBinder({
  (system: HasPeripheryDebugModuleImp) => {
    // Debug module reset
    val io_ndreset: Bool = IO(Output(Bool())).suggestName("ndreset")
    io_ndreset := system.debug.get.ndreset

    // JTAG reset
    val sjtag = system.debug.get.systemjtag.get
    val io_sjtag_reset: Bool = IO(Input(Bool())).suggestName("sjtag_reset")
    sjtag.reset := io_sjtag_reset

    (Seq(io_ndreset, io_sjtag_reset), Nil)
  }
})

class WithTLIOPassthrough extends OverrideIOBinder({
  (system: CanHaveMasterTLMemPort) => {
    val io_tl_mem_pins_temp = IO(DataMirror.internal.chiselTypeClone[HeterogeneousBag[TLBundle]](system.mem_tl)).suggestName("tl_slave")
    io_tl_mem_pins_temp <> system.mem_tl
    (Seq(io_tl_mem_pins_temp), Nil)
  }
})

class WithAXIIOPassthrough extends OverrideIOBinder({
  (system: CanHaveMasterAXI4MemPort) => {
    val io_axi_mem_pins_temp = IO(DataMirror.internal.chiselTypeClone[HeterogeneousBag[AXI4Bundle]](system.mem_axi4))
    io_axi_mem_pins_temp <> system.mem_axi4
    (Seq(io_axi_mem_pins_temp), Nil)
  }
})

class WithUARTIOPassthrough extends OverrideIOBinder({
  (system: HasPeripheryUARTModuleImp) => {
    val io_uart_pins_temp = system.uart.zipWithIndex.map { case (dio, i) => IO(dio.cloneType).suggestName(s"uart_$i") }
    (io_uart_pins_temp zip system.uart).foreach { case (io, sysio) =>
      io <> sysio
    }
    (io_uart_pins_temp, Nil)
  }
})

class WithInternalJTAGIOCells extends OverrideLazyIOBinder({
  // (system: HasCoreInternalDebugModuleImp) => {
  (system: BaseSubsystem) => {
    // println(s"WithInternalJTAGIOCells - system.jtagInner = ${system.jtagInner}")
    // system.jtagInner.map { jtag =>
    //   val (port, ios) = IOCell.generateIOFromSignal(jtag, s"internal_jtag", system.p(IOCellKey), abstractResetAsAsync = true)
    //   (Seq(port), Nil)
    // }.get

    // val inner = system.jtagInner.get
    // val jtag = IO(new VexJTAGChipIO).suggestName("jtag")
    // jtag <> inner
    // (Seq(jtag), Nil)

    implicit val p = GetSystemParameters(system)
    InModuleBody {
      system.asInstanceOf[BaseSubsystem].module match {
        case system: HasCoreInternalDebugModuleImp => {
          println(s"WithInternalJTAGIOCells - system.jtagInner = ${system.jtagInner}")
          val inner = system.jtagInner.get
          val jtag = noPrefix(IO(new VexJTAGChipIO)).suggestName("jtag")
          // val jtag = Wire(new VexJTAGChipIO)
          jtag <> inner
          // IOCell.generateIOFromSignal(jtag, "jtag", p(IOCellKey), abstractResetAsAsync = true)
          println("IOCell.generateIOFromSignal done!")
          (Seq(jtag), Nil)
          // (Nil, Nil)
        }
      }
    }
  }
}
)
