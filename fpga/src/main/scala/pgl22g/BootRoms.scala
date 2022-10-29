package chipyard.fpga.pgl22g

import chipsalliance.rocketchip.config.Config
import freechips.rocketchip.devices.tilelink.BootROMLocated
import freechips.rocketchip.tile.XLen

import sys.process._
import java.io.File

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

class WithVexRiscvBootROM extends Config((site, here, up) => {
  case BootROMLocated(x) => {
    val baseDir = "./generators/vex-riscv/src/main/resources/bootrom"
    val chipyardBootROM = new File(s"$baseDir/bootrom.rv${site(XLen)}.simple.img")
    val clean = s"make -C $baseDir clean"
    require(clean.! == 0 && !chipyardBootROM.exists(), "Failed to clean bootrom!")
    val make = s"make -C $baseDir"
    require(make.! == 0 && chipyardBootROM.exists(), "Failed to build bootrom!")
    up(BootROMLocated(x), site)
      .map(_.copy(contentFileName = chipyardBootROM.getAbsolutePath))
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
      .map(_.copy(contentFileName = bootrom.getAbsolutePath))
  }
})

class WithBareCoreMarkBootROM
(address: BigInt = 0x10000,
 size: Int = 0x10000,
 hang: BigInt = 0x10000, // The hang parameter is used as the power-on reset vector
) extends Config((site, here, up) => {
  case BootROMLocated(x) => {
    val baseDir = "./software/coremark"
    val binary = new File(s"$baseDir/overlay/coremark.bare.bin" + (if (site(XLen) != 32) "64" else ""))
    val clean = s"make -C $baseDir clean"
    require(clean.! == 0 && !binary.exists(), "Failed to clean coremark!")
    val make = s"make -C $baseDir"
    require(make.! == 0 && binary.exists(), "Failed to build coremark!")
    up(BootROMLocated(x), site)
      .map(_.copy(contentFileName = binary.getAbsolutePath, address = address, size = size, hang = hang))
  }
})