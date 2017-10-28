package io.quartz.analyzer.type

import io.quartz.analyzer.Env
import io.quartz.analyzer.mapTypes
import io.quartz.tree.Name

/** A substitution that maps type variables to types */
typealias Subst = Map<Name, TypeK>

// For convenience
val emptySubst: Subst = emptyMap()

infix fun <A, B> Map<A, B>.union(map: Map<A, B>) = map + this

/** Left-associative substitution composition */
infix fun Subst.compose(subst: Subst): Subst = (subst union this).mapValues { apply(it.value, this) }

/** Replaces all free type variables in a scheme with types from a substitution */
fun apply(scheme: SchemeK, subst: Subst): SchemeK = run {
    val substP = scheme.generics.foldRight(subst) { a, b -> b - a.name }
    SchemeK(scheme.generics, apply(scheme.type, substP))
}

/** Replaces all free type variables in a type with types from a substitution */
fun apply(type: TypeK, subst: Subst): TypeK = when (type) {
    is TypeK.Const -> type
    is TypeK.Var -> subst.getOrDefault(type.name, type)
    is TypeK.Apply -> TypeK.Apply(apply(type.t1, subst), apply(type.t2, subst))
}

/** Lazily applies a substitution to all types in an environment */
fun apply(env: Env, subst: Subst): Env = env.mapTypes { apply(it, subst) }

/** Returns all type variables not captured by a scheme's generics */
val SchemeK.freeTypeVariables: Set<Name> get() = type.freeTypeVariables - generics.map { it.name }.toSet()

/** Returns all type variables contained in a type */
val TypeK.freeTypeVariables: Set<Name> get() = when (this) {
    is TypeK.Const -> emptySet()
    is TypeK.Var -> setOf(name)
    is TypeK.Apply -> t1.freeTypeVariables + t2.freeTypeVariables
}
