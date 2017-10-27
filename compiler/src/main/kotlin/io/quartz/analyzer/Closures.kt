package io.quartz.analyzer

import io.quartz.tree.ast.ExprT

/**
 * @author Aedan Smith
 */

val ExprT.freeVariables: Set<String> get() = when (this) {
    is ExprT.Unit,
    is ExprT.Bool -> emptySet()
    is ExprT.Cast -> expr.freeVariables
    is ExprT.Var -> setOf(name)
    is ExprT.Apply -> expr1.freeVariables + expr2.freeVariables
    is ExprT.If -> condition.freeVariables + expr1.freeVariables + expr2.freeVariables
    is ExprT.Lambda -> expr.freeVariables - arg
}
