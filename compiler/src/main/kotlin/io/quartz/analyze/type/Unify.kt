package io.quartz.analyze.type

import io.quartz.err.*
import io.quartz.tree.ir.TypeI
import kategory.*

fun unify(t1: TypeI, t2: TypeI): Result<Subst> = when {
    t1 is TypeI.Apply && t2 is TypeI.Apply -> resultMonad().binding {
        val s1 = unify(t1.t1, t2.t1).bind()
        val s2 = unify(apply(t1.t2, s1), apply(t2.t2, s1)).bind()
        yields(s2 compose s1)
    }.ev()
    t1 is TypeI.Var -> bind(t1, t2)
    t2 is TypeI.Var -> bind(t2, t1)
    t1 is TypeI.Const && t2 is TypeI.Const && t1 == t2 -> emptySubst.right()
    else -> err { "unable to unify $t1 with $t2" }
}

fun bind(tVar: TypeI.Var, type: TypeI) = when {
    tVar == type -> emptySubst.right()
    occursIn(tVar, type) -> err { "infinite constraint $tVar in $type" }
    else -> mapOf(tVar.name to type).right()
}

fun occursIn(tVar: TypeI.Var, type: TypeI): Boolean = when (type) {
    is TypeI.Apply -> occursIn(tVar, type.t1) || occursIn(tVar, type.t2)
    is TypeI.Var -> type == tVar
    is TypeI.Const -> false
}
