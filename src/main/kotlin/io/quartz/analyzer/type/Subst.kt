package io.quartz.analyzer.type

import io.quartz.analyzer.Env
import io.quartz.analyzer.mapTypes

/**
 * @author Aedan Smith
 */

typealias Subst = Map<String, TypeK>

val emptySubst: Subst = emptyMap()

infix fun <A, B> Map<A, B>.union(map: Map<A, B>) = map + this

infix fun Subst.compose(subst: Subst): Subst = (subst union this).mapValues { apply(this, it.value) }

fun apply(subst: Subst, scheme: SchemeK): SchemeK = run {
    val substP = scheme.generics.foldRight(subst) { a, b -> b - a.name }
    SchemeK(scheme.generics, apply(substP, scheme.type))
}

fun apply(subst: Subst, type: TypeK): TypeK = when (type) {
    is TypeK.Const -> type
    is TypeK.Var -> subst.getOrDefault(type.name, type)
    is TypeK.Apply -> TypeK.Apply(apply(subst, type.t1), apply(subst, type.t2))
}

fun apply(subst: Subst, env: Env): Env = env.mapTypes { apply(subst, it) }

val SchemeK.freeTypeVariables: Set<String> get() = type.freeTypeVariables - generics.map { it.name }.toSet()

val TypeK.freeTypeVariables: Set<String> get() = when (this) {
    is TypeK.Const -> emptySet()
    is TypeK.Var -> setOf(name)
    is TypeK.Apply -> t1.freeTypeVariables + t2.freeTypeVariables
}
