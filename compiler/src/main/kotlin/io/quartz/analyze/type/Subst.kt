package io.quartz.analyze.type

import io.quartz.analyze.tree.*
import io.quartz.env.*
import io.quartz.tree.util.Name

typealias Subst = Map<Name, TypeK>

val emptySubst: Subst = emptyMap()

infix fun <A, B> Map<A, B>.union(map: Map<A, B>) = map + this

infix fun Subst.compose(subst: Subst): Subst = (subst union this).mapValues { apply(it.value, this) }

fun apply(scheme: SchemeK, subst: Subst): SchemeK = run {
    val substP = scheme.foralls.fold(subst) { a, b -> a - b }
    SchemeK(scheme.foralls, scheme.constraints, apply(scheme.type, substP))
}

fun apply(type: TypeK, subst: Subst): TypeK = when (type) {
    is TypeK.Const -> type
    is TypeK.Var -> subst.getOrDefault(type.name, type)
    is TypeK.Apply -> TypeK.Apply(apply(type.t1, subst), apply(type.t2, subst))
}

fun apply(env: Env, subst: Subst): Env = env.mapTypes { _, it ->
    it?.map { apply(it, subst) }
}

fun apply(typeInfo: TypeInfo, subst: Subst): TypeInfo = typeInfo.copy(
        scheme = apply(typeInfo.scheme, subst)
)

val TypeK.freeTypeVariables: Set<Name> get() = when (this) {
    is TypeK.Const -> emptySet()
    is TypeK.Var -> setOf(name)
    is TypeK.Apply -> t1.freeTypeVariables + t2.freeTypeVariables
}
