package io.quartz.analyze.tree

import io.quartz.analyze.type.*
import io.quartz.env.*
import io.quartz.err.*
import io.quartz.nil
import io.quartz.tree.ast.*
import io.quartz.tree.ir.*
import io.quartz.tree.util.*
import kategory.*

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
        val type get() = Apply(Apply(function, t1), t2)
        override fun toString() = "($t1 -> $t2)"
    }

    val arrow get() =
        if (this is TypeK.Apply && t1 is TypeK.Apply && t1.t1 == TypeK.function)
            TypeK.Arrow(t1.t2, t2).right()
        else
            err { "expected function, found $this" }

    fun apply(type: TypeK) = TypeK.Apply(this, type)

    val scheme get() = SchemeK(emptySet(), nil, this)

    companion object {
        private val QualifiedName.typeK get() = TypeK.Const(this)

        val bool = "quartz.lang.Bool".qualifiedName.typeK
        val any = "quartz.lang.Any".qualifiedName.typeK
        val function = "quartz.lang.Function".qualifiedName.typeK
    }
}

fun ConstraintT.constraintK(env: Env) = type
        ?.typeK(env)
        ?.map { ConstraintK(it.scheme.instantiate(), name) }
        ?: ConstraintK(null, name).right()

fun SchemeT.schemeK(env: Env) = resultMonad().binding {
    val localEnv = foralls.localEnv(env)
    yields(SchemeK(foralls, constraints.map { it.constraintK(env).bind() }, type.typeK(localEnv).bind()))
}.ev()

fun TypeT.typeK(env: Env): Result<TypeK> = when (this) {
    is TypeT.Id -> env
            .getTypeOrErr(name)
            .map { it.scheme.instantiate() }
            .qualify()
    is TypeT.Apply -> resultMonad()
            .tupled(t1.typeK(env), t2.typeK(env))
            .map { (a, b) -> TypeK.Apply(a, b) }.ev()
            .qualify()
}

val ConstraintK.constraintI get() = ConstraintI(name, TypeI.any)

val SchemeK.schemeI get() = SchemeI(foralls, constraints.map { it.constraintI }, type.typeI)

val TypeK.typeI: TypeI get() = when (this) {
    is TypeK.Const -> TypeI.Const(name)
    is TypeK.Var -> TypeI.Var(name)
    is TypeK.Apply -> TypeI.Apply(t1.typeI, t2.typeI)
}

val TypeK.Arrow.arrowI: TypeI.Arrow get() = TypeI.Arrow(t1.typeI, t2.typeI)
