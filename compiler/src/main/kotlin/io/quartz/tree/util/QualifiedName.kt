package io.quartz.tree.util

import io.quartz.nil

data class QualifiedName(val qualifier: Qualifier, val string: String) {
    val locatableString = (qualifier + string).joinToString(prefix = "", postfix = "", separator = "/")
    val qualifiedString = (qualifier + string).joinToString(prefix = "", postfix = "", separator = ".")
    override fun toString() = qualifiedString
}

typealias Qualifier = List<String>

fun Name.qualify(q: Qualifier) = QualifiedName(q, string)
val Name.qualifiedLocal get() = qualify(nil)

val List<String>.qualifiedName get() = QualifiedName(dropLast(1), last())
val String.qualifiedName get() = split('.').qualifiedName
val Class<*>.qualifiedName get() = typeName.qualifiedName
