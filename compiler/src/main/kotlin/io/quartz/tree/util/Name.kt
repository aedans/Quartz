package io.quartz.tree.util

data class Name(val string: String)

val QualifiedName.unqualified get() = Name(string)

val String.name get() = Name(this)
