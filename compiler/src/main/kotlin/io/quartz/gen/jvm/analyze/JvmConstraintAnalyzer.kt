package io.quartz.gen.jvm.analyze

import io.quartz.gen.jvm.tree.*
import io.quartz.tree.ir.ConstraintI

fun ConstraintI.jvm() = JvmConstraint(name, JvmType.Class(constraint))
