package io.quartz.tree.ast

import io.quartz.tree.Qualifier

/**
 * @author Aedan Smith
 */

data class FileT(
        val `package`: Qualifier,
        val imports: List<Qualifier>,
        val decls: List<DeclT>
)
