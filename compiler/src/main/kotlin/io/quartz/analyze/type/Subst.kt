package io.quartz.analyze.type

import io.quartz.env.*
import io.quartz.tree.ir.*
import io.quartz.tree.util.Name

typealias Subst = Map<Name, TypeI>

val emptySubst: Subst = emptyMap()

infix fun <A, B> Map<A, B>.union(map: Map<A, B>) = map + this

infix fun Subst.compose(subst: Subst): Subst = (subst union this).mapValues { apply(it.value, this) }

fun apply(scheme: SchemeI, subst: Subst): SchemeI = run {
    val substP = scheme.foralls.fold(subst) { a, b -> a - b }
    SchemeI(scheme.foralls, scheme.constraints, apply(scheme.type, substP))
}

fun apply(type: TypeI, subst: Subst): TypeI = when (type) {
    is TypeI.Const -> type
    is TypeI.Var -> subst.getOrDefault(type.name, type)
    is TypeI.Apply -> TypeI.Apply(apply(type.t1, subst), apply(type.t2, subst))
}

fun apply(env: Env, subst: Subst): Env = env.mapTypes { _, it ->
    it?.map { apply(it, subst) }
}

val TypeI.freeTypeVariables: Set<Name> get() = when (this) {
    is TypeI.Const -> emptySet()
    is TypeI.Var -> setOf(name)
    is TypeI.Apply -> t1.freeTypeVariables + t2.freeTypeVariables
}
