package io.quartz.gen.jvm.analyze

import io.quartz.gen.jvm.tree.JvmScheme
import io.quartz.tree.ir.SchemeI

fun SchemeI.jvm() = JvmScheme(foralls, constraints.map { it.jvm() }, type.jvm())
