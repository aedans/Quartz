package io.quartz.gen.jvm.analyze

import io.quartz.gen.jvm.tree.*
import io.quartz.tree.ir.SchemeI

fun SchemeI.jvm() = JvmScheme(constraints.map { JvmGeneric(it.name, it.constraint.jvm()) }, type.jvm())
