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
            val constraints: List<ConstraintT>,
            val decls: List<DeclT>
    ) : DeclT() {
        override fun toString() = "def $name $constraints {\n${decls.joinToString(separator = "\n", prefix = "", postfix = "")}\n}"
    }

    data class Value(
            override val name: Name,
            override val location: Location,
            val schemeT: SchemeT?,
            val expr: ExprT
    ) : DeclT() {
        override fun toString() = "def $name :: $schemeT = $expr"
    }

    data class Abstract(
            override val name: Name,
            override val location: Location,
            val schemeT: SchemeT
    ) : DeclT() {
        override fun toString() = "def $name :: $schemeT"
    }
}
