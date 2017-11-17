package io.quartz.tree.ast

import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.Qualifier

/** Class representing a file's AST */
data class FileT(
        val p: Package,
        val imports: List<ImportT>,
        val decls: List<DeclT>
)

sealed class ImportT {
    data class Qualified(
            val qualifiedName: QualifiedName,
            val alias: Name
    ) : ImportT()

    data class Star(val qualifier: Qualifier) : ImportT()
}

typealias Package = Qualifier
