package io.quartz.analyze

import io.quartz.nil
import io.quartz.tree.ast.ExprT
import io.quartz.tree.util.*

val ExprT.freeVariables: Set<Name> get() = when (this) {
    is ExprT.Cast -> expr.freeVariables
    is ExprT.Var -> if (name.qualifier == nil) setOf(name.unqualified) else emptySet()
    is ExprT.Apply -> expr1.freeVariables + expr2.freeVariables
    is ExprT.If -> condition.freeVariables + expr1.freeVariables + expr2.freeVariables
    is ExprT.Lambda -> expr.freeVariables - argName
}
