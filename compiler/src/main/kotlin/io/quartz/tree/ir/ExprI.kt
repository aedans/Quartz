package io.quartz.tree.ir

import io.quartz.tree.util.*
import kategory.Tuple3

sealed class ExprI : Locatable {
    data class Var(
            override val location: Location?,
            val name: QualifiedName,
            val type: TypeI
    ) : ExprI()

    data class Invoke(
            override val location: Location?,
            val expr1: ExprI,
            val expr2: ExprI,
            val arrow: TypeI.Arrow
    ) : ExprI()

    data class If(
            override val location: Location?,
            val condition: ExprI,
            val expr1: ExprI,
            val expr2: ExprI
    ) : ExprI()

    data class Lambda(
            override val location: Location?,
            val qualifier: Qualifier,
            val constraints: List<ConstraintI>,
            val argName: Name,
            val argType: TypeI,
            val returnType: TypeI,
            val expr: ExprI,
            val closures: List<Tuple3<Name, ExprI, TypeI>>
    ) : ExprI()
}
