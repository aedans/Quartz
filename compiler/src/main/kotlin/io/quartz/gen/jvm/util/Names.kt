package io.quartz.gen.jvm.util

import io.quartz.tree.ir.TypeI
import io.quartz.tree.util.*

val QualifiedName.varClassName get() = copy(string = "_$string")

val varGetterName = "get".name

val Class<*>.typeI get() = TypeI.Const(qualifiedName)
