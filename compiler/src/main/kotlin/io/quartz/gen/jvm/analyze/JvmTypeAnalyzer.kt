package io.quartz.gen.jvm.analyze

import io.quartz.gen.jvm.tree.JvmType
import io.quartz.tree.ir.TypeI
import org.funktionale.collections.prependTo

fun TypeI.jvm(acc: List<JvmType> = emptyList()): JvmType = when (this) {
    is TypeI.Const -> jvm(acc)
    is TypeI.Var -> jvm(acc)
    is TypeI.Apply -> jvm(acc)
}

fun TypeI.Const.jvm(acc: List<JvmType>) = JvmType.Class(name, acc)
fun TypeI.Var.jvm(@Suppress("UNUSED_PARAMETER") acc: List<JvmType>) = JvmType.Generic(name)
fun TypeI.Apply.jvm(acc: List<JvmType>) = t1.jvm(acc = t2.jvm().prependTo(acc))

fun TypeI.Arrow.jvm() = JvmType.Arrow(t1.jvm(), t2.jvm())
