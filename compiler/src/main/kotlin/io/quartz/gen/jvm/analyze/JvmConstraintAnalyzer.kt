package io.quartz.gen.jvm.analyze

import io.quartz.gen.jvm.tree.*
import io.quartz.tree.ir.ConstraintI

fun ConstraintI.jvm() = JvmGeneric(name, constraint.jvm()
        .let { if (it == JvmType.any) JvmType.`object` else it })
