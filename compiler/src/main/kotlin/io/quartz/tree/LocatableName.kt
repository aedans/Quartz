package io.quartz.tree

/**
 * @author Aedan Smith
 */

data class LocatableName(val qualifier: Qualifier, val name: String) {
    override fun toString() = (qualifier + name).joinToString(prefix = "", postfix = "", separator = "/")
}

val QualifiedName.locatableName get() = LocatableName(qualifier, name)

val Class<*>.locatableName get() = qualifiedName.locatableName