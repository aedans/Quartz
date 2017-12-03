package io.quartz.analyze.type

import io.quartz.env.*
import io.quartz.err.*
import io.quartz.nil
import io.quartz.tree.ast.*
import io.quartz.tree.ir.*
import io.quartz.tree.util.*
import kategory.*

data class ConstraintK(val type: TypeK, val name: Name) {
    companion object {
        operator fun invoke(type: TypeK?, name: Name) = ConstraintK(type ?: TypeK.any, name)
    }
}

data class SchemeK(val constraints: List<ConstraintK>, val type: TypeK)

sealed class TypeK {
    data class Const(val name: QualifiedName) : TypeK() {
        override fun toString() = name.toString()
    }

    data class Var(val name: Name) : TypeK() {
        override fun toString() = name.toString()
    }

    data class Apply(val t1: TypeK, val t2: TypeK) : TypeK() {
        override fun toString() = arrow.fold({ "($t1 $t2)" }, { it.toString() })
    }

    data class Arrow(val t1: TypeK, val t2: TypeK) {
        val type get() = TypeK.Apply(TypeK.Apply(TypeK.function, t1), t2)
        override fun toString() = "($t1 -> $t2)"
    }

    companion object {
        val bool = "quartz.lang.Bool".qualifiedName.typeK
        val any = "quartz.lang.Any".qualifiedName.typeK
        val function = "quartz.lang.Function".qualifiedName.typeK
    }
}

val TypeK.arrow get() =
    if (this is TypeK.Apply && t1 is TypeK.Apply && t1.t1 == TypeK.function)
        TypeK.Arrow(t1.t2, t2).right()
    else
        err { "expected function, found $this" }

fun TypeK.apply(type: TypeK) = TypeK.Apply(this, type)

val Name.tVar get() = TypeK.Var(this)

val QualifiedName.typeK get() = TypeK.Const(this)

val TypeK.scheme get() = SchemeK(nil, this)

fun TypeK.generalize(env: Env, subst: Subst) = SchemeK(
        freeTypeVariables.filterNot { name ->
            env.getTypeOrErr(name.qualifiedLocal).bimap(
                    { false },
                    { it.scheme.constraints.any { it.name == name } }
            ).fold(::identity, ::identity)
        }.map {
            ConstraintK(subst[it], it)
        },
        this
)

fun SchemeK.instantiate(env: Env): TypeK = run {
    val (namesP, _) = constraints.fold(emptyList<TypeK>() to env) { (b, env), _ ->
        val name = fresh()
        val type = TypeK.Var(name)
        (b + type) to env.withType(name.qualifiedLocal) { nil.left() }
    }
    val namesZ: Subst = (constraints.map { it.name } zip namesP).toMap()
    apply(type, namesZ)
}

fun ConstraintT.constraintK(env: Env) = type
        ?.typeK(env)
        ?.map { ConstraintK(it.scheme.instantiate(env), name) }
        ?: ConstraintK(null, name).right()

fun SchemeT.schemeK(env: Env) = resultMonad().binding {
    val localEnv = constraints.localEnv(env)
    yields(SchemeK(constraints.map { it.constraintK(env).bind() }, type.typeK(localEnv).bind()))
}.ev()

fun List<ConstraintT>.localEnv(env: Env) = fold(env) { envP, generic ->
    envP.withType(generic.name.qualifiedLocal) {
        TypeInfo(TypeK.Var(generic.name).scheme).right()
    }
}

fun TypeT.typeK(env: Env): Result<TypeK> = when (this) {
    is TypeT.Id -> env
            .getTypeOrErr(name)
            .map { it.scheme.instantiate(env) }
            .qualify()
    is TypeT.Apply -> resultMonad()
            .tupled(t1.typeK(env), t2.typeK(env))
            .map { (a, b) -> TypeK.Apply(a, b) }.ev()
            .qualify()
}

val ConstraintK.constraintI get() = ConstraintI(name, TypeI.any)

val SchemeK.schemeI get() = SchemeI(constraints.map { it.constraintI }, type.typeI)

val TypeK.typeI: TypeI get() = when (this) {
    is TypeK.Const -> TypeI.Const(name)
    is TypeK.Var -> TypeI.Var(name)
    is TypeK.Apply -> TypeI.Apply(t1.typeI, t2.typeI)
}

val TypeK.Arrow.arrowI: TypeI.Arrow get() = TypeI.Arrow(t1.typeI, t2.typeI)
