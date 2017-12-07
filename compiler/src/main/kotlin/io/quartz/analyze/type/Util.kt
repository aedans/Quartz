package io.quartz.analyze.type

import io.quartz.analyze.tree.*
import io.quartz.env.*
import io.quartz.nil
import io.quartz.tree.util.*
import kategory.right

fun TypeK.generalize() = SchemeK(
        freeTypeVariables,
        nil,
        this
)

fun Set<Name>.localEnv(env: Env) = fold(env) { envP, name ->
    envP.withType(name.qualifiedLocal) {
        TypeK.Var(name).right()
    }
}

fun SchemeK.instantiate(): TypeK = run {
    val namesP = foralls.fold(emptyList<TypeK>()) { b, _ ->
        val name = fresh()
        val type = TypeK.Var(name)
        (b + type)
    }
    val namesZ: Subst = (foralls zip namesP).toMap()
    io.quartz.analyze.type.apply(type, namesZ)
}
