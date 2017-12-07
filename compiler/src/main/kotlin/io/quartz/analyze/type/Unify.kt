package io.quartz.analyze.type

import io.quartz.analyze.tree.TypeK
import io.quartz.err.*
import kategory.*

fun unify(t1: TypeK, t2: TypeK): Result<Subst> = when {
    t1 is TypeK.Apply && t2 is TypeK.Apply -> resultMonad().binding {
        val s1 = unify(t1.t1, t2.t1).bind()
        val s2 = unify(apply(t1.t2, s1), apply(t2.t2, s1)).bind()
        yields(s2 compose s1)
    }.ev()
    t1 is TypeK.Var -> bind(t1, t2)
    t2 is TypeK.Var -> bind(t2, t1)
    t1 is TypeK.Const && t2 is TypeK.Const && t1 == t2 -> emptySubst.right()
    else -> err { "unable to unify $t1 with $t2" }
}

fun bind(tVar: TypeK.Var, type: TypeK) = when {
    tVar == type -> emptySubst.right()
    occursIn(tVar, type) -> err { "infinite constraint $tVar in $type" }
    else -> mapOf(tVar.name to type).right()
}

fun occursIn(tVar: TypeK.Var, type: TypeK): Boolean = when (type) {
    is TypeK.Apply -> occursIn(tVar, type.t1) || occursIn(tVar, type.t2)
    is TypeK.Var -> type == tVar
    is TypeK.Const -> false
}
