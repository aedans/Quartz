package io.quartz.gen.jvm.tree

import io.quartz.tree.util.Name

sealed class JvmDecl {
    data class Method(
            val name: Name,
            val foralls: Set<Name>,
            val argTypes: List<JvmType>,
            val returnType: JvmType,
            val expr: JvmExpr? = null,
            val isStatic: Boolean = false
    ) : JvmDecl() {
        val isAbstract get() = expr == null
    }

    data class Field(
            val name: Name,
            val type: JvmType
    ) : JvmDecl()
}
