package io.quartz.gen.jvm.tree

import io.quartz.gen.jvm.sym.JvmMemLoc
import io.quartz.tree.util.Name
import kategory.Tuple3

sealed class JvmExpr {
    data class Var(
            val memLoc: JvmMemLoc,
            val type: JvmType
    ) : JvmExpr()

    data class Invoke(
            val expr1: JvmExpr,
            val expr2: JvmExpr,
            val arrow: JvmType.Arrow
    ) : JvmExpr()

    data class If(
            val condition: JvmExpr,
            val expr1: JvmExpr,
            val expr2: JvmExpr
    ) : JvmExpr()

    data class Lambda(
            val foralls: Set<Name>,
            val argType: JvmType,
            val returnType: JvmType,
            val expr: JvmExpr,
            val closures: List<Tuple3<Name, JvmExpr, JvmType>>
    ) : JvmExpr()
}
