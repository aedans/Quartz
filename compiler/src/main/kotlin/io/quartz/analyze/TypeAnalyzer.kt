package io.quartz.analyze

import io.quartz.analyze.type.*
import io.quartz.env.*
import io.quartz.err.*
import io.quartz.tree.ast.*
import io.quartz.tree.ir.*
import io.quartz.tree.util.qualifiedLocal
import kategory.*

fun ConstraintT.constraintI(env: Env) = env
        .getTraitOrErr(constraint.qualifiedLocal)
        .map { ConstraintI(it.name, name) }

fun SchemeT.schemeI(env: Env) = resultMonad().binding {
    val localEnv = foralls.localEnv(env)
    yields(SchemeI(foralls, constraints.map { it.constraintI(env).bind() }, type.typeI(localEnv).bind()))
}.ev()

fun TypeT.typeI(env: Env): Result<TypeI> = when (this) {
    is TypeT.Id -> env
            .getTypeOrErr(name)
            .map { it.scheme.instantiate() }
            .qualify()
    is TypeT.Apply -> resultMonad()
            .tupled(t1.typeI(env), t2.typeI(env))
            .map { (a, b) -> TypeI.Apply(a, b) }.ev()
            .qualify()
}
