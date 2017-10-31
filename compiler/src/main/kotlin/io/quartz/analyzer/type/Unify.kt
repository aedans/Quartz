package io.quartz.analyzer.type

import io.quartz.analyzer.CompilerError
import io.quartz.analyzer.EitherE
import io.quartz.analyzer.monadErrorE
import kategory.*

class UnableToUnify(t1: TypeK, t2: TypeK) : CompilerError("Unable to unify $t1 with $t2")
class InfiniteBind(tVar: TypeK.Var, type: TypeK) : CompilerError("Infinite type $tVar in $type")

/** Unifies two types if possible, returning a substitution that, when applied to both types, yields the same type */
fun unify(t1: TypeK, t2: TypeK): EitherE<Subst> = when {
    t1 is TypeK.Apply && t2 is TypeK.Apply -> Either.monadErrorE().binding {
        val s1 = unify(t1.t1, t2.t1).bind()
        val s2 = unify(apply(t1.t2, s1), apply(t2.t2, s1)).bind()
        yields(s2 compose s1)
    }.ev()
    t1 is TypeK.Var -> bind(t1, t2)
    t2 is TypeK.Var -> bind(t2, t1)
    t1 is TypeK.Const && t2 is TypeK.Const && t1 == t2 -> emptySubst.right()
    else -> UnableToUnify(t1, t2).left()
}

/** Returns a substitution in which a type variable is bound to a type */
fun bind(tVar: TypeK.Var, type: TypeK) = when {
    tVar == type -> emptySubst.right()
    occursIn(tVar, type) -> InfiniteBind(tVar, type).left()
    else -> mapOf(tVar.name to type).right()
}

/** Returns true if a type variable is contained inside a type */
fun occursIn(tVar: TypeK.Var, type: TypeK): Boolean = when (type) {
    is TypeK.Apply -> occursIn(tVar, type.t1) || occursIn(tVar, type.t2)
    is TypeK.Var -> type == tVar
    is TypeK.Const -> false
}
