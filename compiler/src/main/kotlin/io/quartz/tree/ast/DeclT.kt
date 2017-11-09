package io.quartz.tree.ast

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name

/** Sealed class representing all AST declarations */
sealed class DeclT : Locatable {
    data class Interface(
            override val location: Location,
            val name: Name,
            val constraints: List<ConstraintT>,
            val abstracts: List<Abstract>
    ) : DeclT() {
        data class Abstract(
                val name: Name,
                val location: Location,
                val schemeT: SchemeT
        ) {
            override fun toString() = "def $name :: $schemeT"
        }

        override fun toString() = "def $name $constraints {\n${abstracts.joinToString(separator = "\n", prefix = "", postfix = "")}\n}"
    }

    data class Value(
            override val location: Location,
            val name: Name,
            val schemeT: SchemeT?,
            val expr: ExprT
    ) : DeclT() {
        override fun toString() = "def $name :: $schemeT = $expr"
    }

}
