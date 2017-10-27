package io.quartz.tree.ast

import io.quartz.tree.Locatable
import io.quartz.tree.Location

/**
 * @author Aedan Smith
 */

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

    data class Var(
            override val location: Location,
            val name: String
    ) : ExprT() {
        override fun toString() = name
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
            val arg: String,
            val expr: ExprT
    ) : ExprT() {
        override fun toString() = "\\$arg = $expr"
    }
}
