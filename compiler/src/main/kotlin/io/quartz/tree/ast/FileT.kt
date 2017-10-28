package io.quartz.tree.ast

import io.quartz.tree.Qualifier

/** Class representing a file's AST */
data class FileT(
        val `package`: Qualifier,
        val imports: List<Qualifier>,
        val decls: List<DeclT>
)
