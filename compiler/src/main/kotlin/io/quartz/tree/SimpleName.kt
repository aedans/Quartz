package io.quartz.tree

/** Class for representing local or unresolved identifiers */
data class SimpleName(val string: String) {
    fun capitalize() = string.capitalize().name
    override fun toString() = string
}

// For convenience
typealias Name = SimpleName

val QualifiedName.unqualified get() = Name(string)

val String.name get() = Name(this)

fun Class<*>.name() = simpleName.name
