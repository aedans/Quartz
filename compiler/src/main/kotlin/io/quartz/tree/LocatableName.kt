package io.quartz.tree

/** Class for representing identifiers in the form p1/p2/Name */
data class LocatableName(val qualifier: Qualifier, val name: String) {
    override fun toString() = (qualifier + name).joinToString(prefix = "", postfix = "", separator = "/")
}

val QualifiedName.locatableName get() = LocatableName(qualifier, name)

val Class<*>.locatableName get() = qualifiedName.locatableName