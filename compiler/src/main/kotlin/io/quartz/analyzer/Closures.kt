package io.quartz.analyzer

import io.quartz.tree.Name
import io.quartz.tree.ast.ExprT

val ExprT.freeVariables: Set<Name> get() = when (this) {
    is ExprT.Unit,
    is ExprT.Bool -> emptySet()
    is ExprT.Cast -> expr.freeVariables
    is ExprT.Var -> setOf(name)
    is ExprT.Apply -> expr1.freeVariables + expr2.freeVariables
    is ExprT.If -> condition.freeVariables + expr1.freeVariables + expr2.freeVariables
    is ExprT.Lambda -> expr.freeVariables - arg
}
