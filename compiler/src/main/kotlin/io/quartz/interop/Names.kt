package io.quartz.interop

import io.quartz.tree.*

fun Name.capitalizeOrUnderscore() = (if (!string.first().isLowerCase()) "_$string" else string.capitalize()).name

fun Name.varClassName() = "\$Get${capitalizeOrUnderscore()}".name
fun QualifiedName.varClassName() = unqualified.varClassName().qualify(qualifier)

fun Name.varGetterName() = "get${capitalizeOrUnderscore()}".name
