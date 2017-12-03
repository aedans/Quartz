package io.quartz.gen.jvm.tree

import io.quartz.tree.util.QualifiedName

data class JvmClass(
        val name: QualifiedName,
        val decls: List<JvmDecl>,
        val generics: List<JvmGeneric> = emptyList(),
        val interfaces: List<JvmType> = emptyList(),
        val annotations: List<JvmAnnotation> = emptyList(),
        val isInterface: Boolean = false,
        val isFinal: Boolean = false
)
