package io.quartz.interop

import io.quartz.tree.*

fun Name.varClassName() = "\$get_$string".name
fun QualifiedName.varClassName() = unqualified.varClassName().qualify(qualifier)

fun Name.varGetterName() = "get_$string".name
