package pgl22g

import chipsalliance.rocketchip.config.Config
import freechips.rocketchip.devices.tilelink.BootROMLocated
import freechips.rocketchip.tile.XLen

import sys.process._
import java.io.File
import pgl22g._
object BootRoms {
  def onChipCoreMark: String = {
    val binaryFile = new File("./software/coremark/overlay/coremark.perf.bin")
    // val clean = s"make -C ./software/coremark/riscv-coremark/perf clean"
    // require(clean.! == 0 && !binaryFile.exists(), "failed to clean coremark for vex-riscv!")
    val make = s"make -C ./software/coremark/riscv-coremark/perf"
    if (!binaryFile.exists()) require(make.! == 0, "failed to make coremark for vex-riscv!")
    binaryFile.getAbsolutePath
  }
}

class WithoutBootROM extends Config((site, here, up) => {
  case BootROMLocated(x) => None
})

class WithPicoRVBootROM extends Config((site, here, up) => {
  case BootROMLocated(x) => {
    val baseDir = "./generators/picorv/src/main/resources/bootrom"
    val chipyardBootROM = new File(s"$baseDir/start.rv${site(XLen)}.img")
    val clean = s"make -C $baseDir clean"
    require(clean.! == 0 && !chipyardBootROM.exists(), "Failed to clean bootrom!")
    val make = s"make -C $baseDir"
    require(make.! == 0 && chipyardBootROM.exists(), "Failed to build bootrom!")
    up(BootROMLocated(x), site)
      .map(_.copy(contentFileName = chipyardBootROM.getAbsolutePath))
  }
})

object CoreMarkBootROM {
  def make(): String = {
    val baseDir = "./software/coremark"
    val binary = new File(s"$baseDir/overlay/coremark.bare.bin")
    val clean = s"make -C $baseDir clean"
    require(clean.! == 0 && !binary.exists(), "Failed to clean coremark!")
    val make = s"make -C $baseDir"
    require(make.! == 0 && binary.exists(), "Failed to build coremark!")
    binary.getAbsolutePath
  }
}

class WithVexRiscvBootROM extends Config((site, here, up) => {
  case BootROMLocated(x) => {
    require(site(XLen) == 32)
    val baseDir = "./generators/vex-riscv/src/main/resources/bootrom"
    val binary = new File(s"$baseDir/bootrom.rv${site(XLen)}.img")
    // val clean = s"make -C $baseDir clean"
    // require(clean.! == 0 && !binary.exists(), "Failed to clean bootrom!")
    val make = s"make -C $baseDir"
    require(make.! == 0 && binary.exists(), "Failed to build bootrom!")
    up(BootROMLocated(x), site)
      .map(_.copy(contentFileName = binary.getAbsolutePath))
  }
})

class WithTestsBootROM extends Config((site, here, up) => {
  case BootROMLocated(x) => {
    val baseDir = "./software/tests"
    val bootrom = new File(s"$baseDir/start.rv${site(XLen)}.img")
    val clean = s"make -C $baseDir clean"
    require(clean.! == 0 && !bootrom.exists(), "Failed to clean bootrom!")
    val make = s"make -C $baseDir"
    require(make.! == 0 && bootrom.exists(), "Failed to build bootrom!")
    up(BootROMLocated(x), site)
      .map(_.copy(contentFileName = bootrom.getAbsolutePath, hang = 0x10000))
  }
})

class WithBareCoreMarkBootROM
(address: BigInt = 0x10000,
 size: Int = 0x10000,
 hang: BigInt = 0x10000, // The hang parameter is used as the power-on reset vector
) extends Config((site, here, up) => {
  case BootROMLocated(x) => {
    up(BootROMLocated(x), site)
      .map(_.copy(contentFileName = CoreMarkBootROM.make(), address = address, size = size, hang = hang))
  }
})