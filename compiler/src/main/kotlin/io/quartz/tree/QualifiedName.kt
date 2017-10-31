package io.quartz.tree

/** Class for representing identifiers in the form p1.p2.Name */
data class QualifiedName(val qualifier: Qualifier, val string: String) {
    override fun toString() = (qualifier + string).joinToString(prefix = "", postfix = "", separator = ".")
}

/** Represents the package that qualifies a string */
typealias Qualifier = List<String>

val QualifiedName.name get() = string.name

val List<String>.qualifiedName get() = QualifiedName(dropLast(1), last())

fun Name.qualify(qualifier: Qualifier) = QualifiedName(qualifier, string)
val Name.qualifiedLocal get() = qualify(nil)

val LocatableName.qualifiedName get() = QualifiedName(qualifier, string)

val Class<*>.qualifiedName get() = typeName
        .split('.')
        .let { QualifiedName(it.dropLast(1), it.last()) }
