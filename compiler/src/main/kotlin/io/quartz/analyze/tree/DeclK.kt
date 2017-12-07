package io.quartz.analyze.tree

import io.quartz.tree.util.QualifiedName

sealed class DeclK {
    data class Trait(
            val qualifiedName: QualifiedName
    ) : DeclK()

    data class Value(
            val schemeK: SchemeK
    ) : DeclK()

    data class Instance(
            val instance: QualifiedName
    ) : DeclK()
}
