package io.quartz.tree

/** Class for representing identifiers in the form p1/p2/Name */
data class LocatableName(val qualifier: Qualifier, val string: String) {
    override fun toString() = (qualifier + string).joinToString(prefix = "", postfix = "", separator = "/")
}

val QualifiedName.locatableName get() = LocatableName(qualifier, string)

val Class<*>.locatableName get() = qualifiedName.locatableName