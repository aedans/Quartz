package io.quartz.gen.jvm.analyze

import io.quartz.gen.jvm.sym.*
import io.quartz.gen.jvm.tree.JvmExpr
import io.quartz.tree.ir.ExprI
import io.quartz.tree.util.*
import kategory.Tuple3

fun ExprI.jvm(symTable: JvmSymTable): JvmExpr = when (this) {
    is ExprI.Var -> jvm(symTable)
    is ExprI.Invoke -> jvm(symTable)
    is ExprI.If -> jvm(symTable)
    is ExprI.Lambda -> jvm(symTable)
}

fun ExprI.Var.jvm(symTable: JvmSymTable) = JvmExpr.Var(symTable.getMemLoc(name), type.jvm())

fun ExprI.Invoke.jvm(symTable: JvmSymTable) = JvmExpr.Invoke(expr1.jvm(symTable), expr2.jvm(symTable), arrow.jvm())

fun ExprI.If.jvm(symTable: JvmSymTable) = JvmExpr.If(condition.jvm(symTable), expr1.jvm(symTable), expr1.jvm(symTable))

fun ExprI.Lambda.jvm(symTable: JvmSymTable) = run {
    val localSymTable = symTable
            .mapMemLoc { name, memLoc ->
                when {
                    closures.any { it.a.qualifiedLocal == name } -> JvmMemLoc.LocalField(name.unqualified)
                    else -> memLoc
                }
            }
            .withMemLoc(argName.qualifiedLocal, JvmMemLoc.Arg(0))
    JvmExpr.Lambda(
            constraints.map { it.jvm() },
            argType.jvm(),
            returnType.jvm(),
            expr.jvm(localSymTable),
            closures.map { Tuple3(it.a, it.b.jvm(symTable), it.c.jvm()) }
    )
}
