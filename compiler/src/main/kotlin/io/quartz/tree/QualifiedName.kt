package io.quartz.tree

/**
 * @author Aedan Smith
 */

data class QualifiedName(val qualifier: Qualifier, val name: String) {
    override fun toString() = (qualifier + name).joinToString(prefix = "", postfix = "", separator = ".")
}

typealias Qualifier = List<String>

fun Name.qualify(qualifier: Qualifier) = QualifiedName(qualifier, string)
val Name.qualifiedLocal get() = qualify(nil)

val Class<*>.qualifiedName get() = typeName
        .split('.')
        .let { QualifiedName(it.dropLast(1), it.last()) }
