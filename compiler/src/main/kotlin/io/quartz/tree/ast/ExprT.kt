package io.quartz.tree.ast

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName

/** Sealed class representing all AST expressions */
sealed class ExprT : Locatable {
    data class Unit(
            override val location: Location
    ) : ExprT() {
        override fun toString() = "()"
    }

    data class Bool(
            override val location: Location,
            val boolean: Boolean
    ) : ExprT() {
        override fun toString() = boolean.toString()
    }

    data class Cast(
            override val location: Location,
            val expr: ExprT,
            val type: TypeT
    ) : ExprT() {
        override fun toString() = "$expr :: $type"
    }

    data class Id(
            override val location: Location,
            val name: QualifiedName
    ) : ExprT() {
        override fun toString() = name.toString()
    }

    data class Apply(
            override val location: Location,
            val expr1: ExprT,
            val expr2: ExprT
    ) : ExprT() {
        override fun toString() = "($expr1) $expr2"
    }

    data class If(
            override val location: Location,
            val condition: ExprT,
            val expr1: ExprT,
            val expr2: ExprT
    ) : ExprT() {
        override fun toString() = "if $condition then $expr1 else $expr2"
    }

    data class Lambda(
            override val location: Location,
            val arg: Name,
            val expr: ExprT
    ) : ExprT() {
        override fun toString() = "\\$arg = $expr"
    }

    data class Dot(
            override val location: Location,
            val expr: ExprT,
            val name: Name
    ) : ExprT() {
        override fun toString() = "$expr.$name"
    }
}
