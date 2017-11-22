package io.quartz.interop

import io.quartz.tree.*

fun Name.varClassName() = "_$this".name
fun QualifiedName.varClassName() = unqualified.varClassName().qualify(qualifier)
fun Name.varGetterName() = "get".name
