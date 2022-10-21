package chisel3.util.experimental

// SPDX-License-Identifier: Apache-2.0

import chisel3._
import chisel3.experimental.{Analog, BaseModule, ChiselAnnotation, RunFirrtlTransform, annotate, attach}
import chisel3.internal.firrtl.Attach
import chisel3.internal.{Builder, InstanceId, NamedComponent, Namespace}
import firrtl.transforms.{DontTouchAnnotation, NoDedupAnnotation}
import firrtl.passes.wiring.{AnalogSinkAnnotation, AnalogSourceAnnotation, AnalogWiringTransform, SinkAnnotation, SourceAnnotation, WiringTransform}
import firrtl.annotations.{ComponentName, ModuleName}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.SyncVar

object AnalogUtils {
  var maps: mutable.HashMap[String, mutable.ListBuffer[Analog]] = new mutable.HashMap()

  def add(wire: Analog, name: String): Unit = {
    // if (!maps.contains(name)) maps.put(name, ListBuffer())
    // maps.get(name) match {
    //   case Some(list) => {
    //     list.append(wire)
    //     val headModules = mutable.ListBuffer[BaseModule]()
    //     val lastModules = mutable.ListBuffer[BaseModule]()
    //     if (list.length == 2) {
    //       // println(s"got pair: ${list}")
    //       // var m = list.head._parent
    //       // while (m.nonEmpty) {
    //       //   println("0 module", m)
    //       //   headModules.append(m.get)
    //       //   m = m.get._parent
    //       // }
    //       // m = list.last._parent
    //       // while (m.nonEmpty) {
    //       //   println("1 module", m)
    //       //   lastModules.append(m.get)
    //       //   m = m.get._parent
    //       // }
    //       // val a = list.head
    //       // val b = list.last
    //       // var aSource = a
    //       // for (i <- 1 until headModules.length) {
    //       //   val mTarget = headModules(i)
    //       //   println(s"add wire $a to module $mTarget")
    //       //   val aClone = a.cloneType.suggestName(name)
    //       //   mTarget.bindIoInPlace(aClone)
    //       //   attach(aClone, aSource)
    //       //   aSource = aClone
    //       // }
    //       // attach(aSource, list.last)
    //       attach(list.last._parent.get.findPort(name).get.asInstanceOf[Analog], list.last)
    //       println("attach done")
    //     } else {
    //       var m = list.head._parent
    //       while (m.nonEmpty) {
    //         println("pre module", m)
    //         headModules.append(m.get)
    //         m = m.get._parent
    //       }
    //       val a = list.head
    //       var aSource = a
    //       println(s"now top module: ${a._parent.get}")
    //       for (i <- 1 until headModules.length) {
    //         val mTarget = headModules(i)
    //         println(s"add wire $a to module $mTarget")
    //         val aClone = a.cloneType.suggestName(name)
    //         mTarget.bindIoInPlace(aClone)
    //         println(s"bind done: ${mTarget.name}.${a.name}")
    //         // attach(aClone, aSource)
    //         aSource = aClone
    //       }
    //       // attach(aSource, list.last)
    //       println("pre attach done")
    //     }
    //   }
    // }
  }
}
