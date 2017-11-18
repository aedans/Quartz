package io.quartz.tree.ast

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName

/** Sealed class representing all AST expressions */
sealed class ExprT : Locatable {
    data class Cast(
            override val location: Location,
            val expr: ExprT,
            val type: TypeT
    ) : ExprT()

    data class Id(
            override val location: Location,
            val name: QualifiedName
    ) : ExprT()

    data class Apply(
            override val location: Location,
            val expr1: ExprT,
            val expr2: ExprT
    ) : ExprT()

    data class If(
            override val location: Location,
            val condition: ExprT,
            val expr1: ExprT,
            val expr2: ExprT
    ) : ExprT()

    data class Lambda(
            override val location: Location,
            val arg: Name,
            val expr: ExprT
    ) : ExprT()
}
