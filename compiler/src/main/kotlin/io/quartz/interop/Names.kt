package io.quartz.interop

import io.quartz.tree.*

fun Name.varClassName() = "\$Get${string.capitalize()}".name
fun QualifiedName.varClassName() = unqualified.varClassName().qualify(qualifier)

fun Name.varGetterName() = "get${string.capitalize()}".name
