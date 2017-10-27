package io.quartz.tree.ast

import io.quartz.tree.Package

/**
 * @author Aedan Smith
 */

data class FileT(
        val imports: List<Package>,
        val decls: List<DeclT>
)
