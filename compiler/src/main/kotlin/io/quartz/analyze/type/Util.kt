package io.quartz.analyze.type

import io.quartz.env.*
import io.quartz.nil
import io.quartz.tree.ir.*
import io.quartz.tree.util.*
import kategory.right

fun TypeI.generalize() = SchemeI(
        freeTypeVariables,
        nil,
        this
)

fun Set<Name>.localEnv(env: Env) = fold(env) { envP, name ->
    envP.withType(name.qualifiedLocal) {
        TypeI.Var(name).right()
    }
}

fun SchemeI.instantiate(): TypeI = run {
    val namesP = foralls.fold(emptyList<TypeI>()) { b, _ ->
        val name = fresh()
        val type = TypeI.Var(name)
        (b + type)
    }
    val namesZ: Subst = (foralls zip namesP).toMap()
    io.quartz.analyze.type.apply(type, namesZ)
}
