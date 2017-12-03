package io.quartz.tree.ast

import io.quartz.tree.util.*

sealed class ExprT : Locatable {
    data class Cast(
            override val location: Location?,
            val expr: ExprT,
            val type: TypeT
    ) : ExprT()

    data class Var(
            override val location: Location?,
            val name: QualifiedName
    ) : ExprT()

    data class Apply(
            override val location: Location?,
            val expr1: ExprT,
            val expr2: ExprT
    ) : ExprT()

    data class If(
            override val location: Location?,
            val condition: ExprT,
            val expr1: ExprT,
            val expr2: ExprT
    ) : ExprT()

    data class Lambda(
            override val location: Location?,
            val argName: Name,
            val expr: ExprT
    ) : ExprT()
}
