package pgl22g.testharness

import chisel3._
import sifive.blocks.devices.uart.UARTPortIO
import sifive.fpgashells.ip.pango.ddr3.PGL22GMIGIODDRBase
import sifive.fpgashells.shell.FlippedJTAGIO
import sifive.fpgashells.shell.pango.PerfUARTIO

trait PGL22GTestHarnessPerfUartImp {
  val uart: PerfUARTIO
  val sys_clock: Clock
}

trait PGL22GTestHarnessUartImp {
  val uart: UARTPortIO
}

trait PGL22GTestHarnessUartTopClockImp extends PGL22GTestHarnessUartImp

trait PGL22GTestHarnessJtagImpl {
  val jtag: FlippedJTAGIO
  val jtagResetN: Bool
}

trait PGL22GTestHarnessDDRImp {
  val ddr: PGL22GMIGIODDRBase
  val ddrphy_rst_done: Bool
  val ddrc_init_done: Bool
  val pll_lock: Bool
  val pll_clk_bus: Clock
  val sysclk: Clock
  val hardResetN: Bool
}

