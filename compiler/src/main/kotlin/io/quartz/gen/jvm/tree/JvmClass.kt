package io.quartz.gen.jvm.tree

import io.quartz.nil
import io.quartz.tree.util.*

data class JvmClass(
        val name: QualifiedName,
        val decls: List<JvmDecl>,
        val foralls: Set<Name> = emptySet(),
        val interfaces: List<JvmType> = nil,
        val annotations: List<JvmAnnotation> = nil,
        val isInterface: Boolean = false,
        val isFinal: Boolean = false
)
