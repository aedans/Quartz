package io.quartz.analyzer.type

/**
 * @author Aedan Smith
 */

class UnableToUnify(t1: TypeK, t2: TypeK) : Exception("Unable to unify ${t1 to t2}")
class InfiniteBind(tVar: TypeK.Var, type: TypeK) : Exception("Infinite bind ${tVar to type}")

fun unify(t1: TypeK, t2: TypeK): Subst = when {
    t1 is TypeK.Apply && t2 is TypeK.Apply -> {
        val s1 = unify(t1.t1, t2.t1)
        val s2 = unify(apply(s1, t1.t2), apply(s1, t2.t2))
        s2 compose s1
    }
    t1 is TypeK.Var -> bind(t1, t2)
    t2 is TypeK.Var -> bind(t2, t1)
    t1 is TypeK.Const && t2 is TypeK.Const && t1 == t2 -> emptyMap()
    else -> throw UnableToUnify(t1, t2)
}

fun bind(tVar: TypeK.Var, type: TypeK): Subst = when {
    tVar == type -> emptyMap()
    occursIn(tVar, type) -> throw InfiniteBind(tVar, type)
    else -> mapOf(tVar.name to type)
}

fun occursIn(tVar: TypeK.Var, type: TypeK): Boolean = when (type) {
    is TypeK.Apply -> occursIn(tVar, type.t1) || occursIn(tVar, type.t2)
    is TypeK.Var -> type == tVar
    is TypeK.Const -> false
}
