package io.quartz.analyze

import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.ExprT
import io.quartz.tree.qualifiedLocal

val ExprT.freeVariables: Set<QualifiedName> get() = when (this) {
    is ExprT.Cast -> expr.freeVariables
    is ExprT.Id -> setOf(name)
    is ExprT.Apply -> expr1.freeVariables + expr2.freeVariables
    is ExprT.If -> condition.freeVariables + expr1.freeVariables + expr2.freeVariables
    is ExprT.Lambda -> expr.freeVariables - arg.qualifiedLocal
}
