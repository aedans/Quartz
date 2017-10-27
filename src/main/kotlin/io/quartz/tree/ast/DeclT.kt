package io.quartz.tree.ast

import io.quartz.tree.Locatable
import io.quartz.tree.Location

/**
 * @author Aedan Smith
 */

sealed class DeclT : Locatable {
    abstract val name: String

    data class Class(
            override val name: String,
            override val location: Location,
            val decls: List<DeclT>
    ) : DeclT() {
        override fun toString() = "def $name {\n${decls.joinToString(separator = "\n", prefix = "", postfix = "")}\n}"
    }

    data class Value(
            override val name: String,
            override val location: Location,
            val type: TypeT?,
            val expr: ExprT
    ) : DeclT() {
        override fun toString() = "def $name :: $type = $expr"
    }
}
