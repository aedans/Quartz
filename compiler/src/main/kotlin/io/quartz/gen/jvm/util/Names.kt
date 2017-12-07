package io.quartz.gen.jvm.util

import io.quartz.analyze.tree.TypeK
import io.quartz.tree.util.*

val QualifiedName.varClassName get() = copy(string = "_$string")

val varGetterName = "get".name

val Class<*>.typeK get() = TypeK.Const(qualifiedName)
