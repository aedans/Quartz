package io.quartz.tree.ast

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name

/** Sealed class representing all AST declarations */
sealed class DeclT : Locatable {
    abstract val name: Name

    data class Class(
            override val name: Name,
            override val location: Location,
            val decls: List<DeclT>
    ) : DeclT() {
        override fun toString() = "def $name {\n${decls.joinToString(separator = "\n", prefix = "", postfix = "")}\n}"
    }

    data class Value(
            override val name: Name,
            override val location: Location,
            val type: TypeT?,
            val expr: ExprT
    ) : DeclT() {
        override fun toString() = "def $name :: $type = $expr"
    }
}
