package chisel3.util.experimental

// SPDX-License-Identifier: Apache-2.0

import chisel3._
import chisel3.experimental.{ChiselAnnotation, RunFirrtlTransform, annotate}
import chisel3.internal.firrtl.Attach
import chisel3.internal.{Builder, InstanceId, NamedComponent, Namespace}
import firrtl.transforms.{DontTouchAnnotation, NoDedupAnnotation}
import firrtl.passes.wiring.{AnalogSinkAnnotation, AnalogSourceAnnotation, AnalogWiringTransform, SinkAnnotation, SourceAnnotation, WiringTransform}
import firrtl.annotations.{ComponentName, ModuleName}

import scala.concurrent.SyncVar

/** An exception related to AnalogUtils
 * @param message the exception message
 */
class AnalogUtilsException(message: String) extends Exception(message)
object AnalogUtils {
  /* A global namespace for Analog ids */
  private val namespace: SyncVar[Namespace] = new SyncVar
  namespace.put(Namespace.empty)

  /* Get a new name (value) from the namespace */
  private def newName(value: String): String = {
    val ns = namespace.take()
    val valuex = ns.name(value)
    namespace.put(ns)
    valuex
  }

  /* True if the requested name (value) exists in the namespace */
  private def checkName(value: String): Boolean = namespace.get.contains(value)

  /** Add a named source cross module reference
   * @param component source circuit component
   * @param name unique identifier for this source
   * @param disableDedup disable deduplication of this source component (this should be true if you are trying to wire
   * from specific identical sources differently)
   * @param uniqueName if true, this will use a non-conflicting name from the global namespace
   * @return the name used
   * @note if a uniqueName is not specified, the returned name may differ from the user-provided name
   */
  def addSource(
                 component:    NamedComponent,
                 name:         String,
                 disableDedup: Boolean = false,
                 uniqueName:   Boolean = false
               ): String = {

    val id = if (uniqueName) { newName(name) }
    else { name }
    val maybeDedup =
      if (disableDedup) { Seq(new ChiselAnnotation { def toFirrtl = NoDedupAnnotation(component.toNamed.module) }) }
      else { Seq[ChiselAnnotation]() }
    val annotations =
      Seq(
        new ChiselAnnotation with RunFirrtlTransform {
          def toFirrtl = AnalogSourceAnnotation(component.toNamed, id)
          def transformClass = classOf[AnalogWiringTransform]
        },
        new ChiselAnnotation { def toFirrtl = DontTouchAnnotation(component.toNamed) }
      ) ++ maybeDedup

    println(s"id: $id")
    annotations.foreach(anno => {
      println(s"anno: ${anno}")
    })
    annotations.foreach(annotate(_))
    id
  }

  /** Add a named sink cross module reference. Multiple sinks may map to the same source.
   * @param component sink circuit component
   * @param name unique identifier for this sink that must resolve to
   * @param disableDedup disable deduplication of this sink component (this should be true if you are trying to wire
   * specific, identical sinks differently)
   * @param forceExists if true, require that the provided `name` parameter already exists in the global namespace
   * @throws AnalogUtilsException if name is expected to exist and it doesn't
   */
  def addSink(
               component:    InstanceId,
               name:         String,
               disableDedup: Boolean = false,
               forceExists:  Boolean = false
             ): Unit = {

    if (forceExists && !checkName(name)) {
      throw new AnalogUtilsException(s"Sink ID '$name' not found in AnalogUtils ID namespace")
    }
    def moduleName = component.toNamed match {
      case c: ModuleName    => c
      case c: ComponentName => c.module
      case _ => throw new ChiselException("Can only add a Module or Component sink", null)
    }
    val maybeDedup =
      if (disableDedup) { Seq(new ChiselAnnotation { def toFirrtl = NoDedupAnnotation(moduleName) }) }
      else { Seq[ChiselAnnotation]() }
    val annotations =
      Seq(new ChiselAnnotation with RunFirrtlTransform {
        def toFirrtl = AnalogSinkAnnotation(component.toNamed, name)
        // def toFirrtl = Builder.pushCommand(Attach())
        def transformClass = classOf[AnalogWiringTransform]
      }) ++ maybeDedup
    annotations.foreach(annotate(_))
  }

  /** Connect a source to one or more sinks
   * @param source a source component
   * @param sinks one or more sink components
   * @return the name of the signal used to connect the source to the
   * sinks
   * @note the returned name will be based on the name of the source
   * component
   */
  def bore(source: Data, sinks: Seq[Data]): String = {
    val AnalogName =
      try {
        source.instanceName
      } catch {
        case _: Exception => "bore"
      }
    val genName = addSource(source, AnalogName, true, true)
    sinks.foreach(addSink(_, genName, true, true))
    genName
  }

}
