package io.quartz.interop

import io.quartz.tree.*

fun Name.varClassName() = "$$this".name
fun QualifiedName.varClassName() = unqualified.varClassName().qualify(qualifier)
fun Name.varGetterName() = this
