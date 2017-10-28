package io.quartz.tree

/**
 * @author Aedan Smith
 */

data class SimpleName(val string: String) {
    fun capitalize() = string.capitalize().name
    override fun toString() = string
}

typealias Name = SimpleName

val QualifiedName.unqualified get() = Name(name)

val String.name get() = Name(this)

fun Class<*>.name() = simpleName.name
