package io.quartz.tree.util

sealed class Import {
    data class Qualified(val name: QualifiedName, val alias: Name) : Import()
    data class Star(val qualifier: Qualifier) : Import()
}
