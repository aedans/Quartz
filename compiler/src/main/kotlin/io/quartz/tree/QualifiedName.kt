package io.quartz.tree

/** Class for representing identifiers in the form p1.p2.Name */
data class QualifiedName(val qualifier: Qualifier, val name: String) {
    override fun toString() = (qualifier + name).joinToString(prefix = "", postfix = "", separator = ".")
}

/** Represents the package that qualifies a name */
typealias Qualifier = List<String>

fun Name.qualify(qualifier: Qualifier) = QualifiedName(qualifier, string)
val Name.qualifiedLocal get() = qualify(nil)

val LocatableName.qualifiedName get() = QualifiedName(qualifier, name)

val Class<*>.qualifiedName get() = typeName
        .split('.')
        .let { QualifiedName(it.dropLast(1), it.last()) }
