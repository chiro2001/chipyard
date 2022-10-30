package pgl22g.onchip

import chipsalliance.rocketchip.config.{Config, Parameters}
import chipyard.{BuildSystem, DigitalTop, DigitalTopModule}
import vexriscv.chipyard.{HasCoreInternalDebug, HasCoreInternalDebugModuleImp}

class OnChipDigitalTop(implicit p: Parameters)
  extends DigitalTop
    with HasCoreInternalDebug {
  override lazy val module = new OnChipDigitalTopModule(this)
}

class OnChipDigitalTopModule[+L <: OnChipDigitalTop](l: L)
  extends DigitalTopModule(l)
    with HasCoreInternalDebugModuleImp


class WithOnChipSystem extends Config((site, here, up) => {
  case BuildSystem => (p: Parameters) => new OnChipDigitalTop()(p)
})