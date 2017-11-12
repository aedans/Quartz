package io.quartz.analyze.type

import io.quartz.analyze.*
import io.quartz.err.Result
import io.quartz.err.err
import io.quartz.err.resultMonad
import io.quartz.interop.schemeK
import io.quartz.nil
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.ConstraintT
import io.quartz.tree.ast.SchemeT
import io.quartz.tree.ast.TypeT
import io.quartz.tree.ir.*
import io.quartz.tree.qualifiedLocal
import kategory.*

/** Class representing a constraintT for compiler analysis */
data class ConstraintK(val type: TypeK, val name: Name) {
    override fun toString() = "($type $name) =>"

    companion object {
        operator fun invoke(type: TypeK?, name: Name) = ConstraintK(type ?: TypeK.any, name)
    }
}

/** Class representing a type scheme for compiler analysis */
data class SchemeK(val constraints: List<ConstraintK>, val type: TypeK) {
    override fun toString() = "$constraints $type"
}

/** Sealed class representing all types for compiler analysis */
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

    /** Convenience class representing a type of (Function t1) t2 */
    data class Arrow(val t1: TypeK, val t2: TypeK) {
        val type get() = TypeK.Apply(TypeK.Apply(TypeK.function, t1), t2)
        override fun toString() = "($t1 -> $t2)"
    }

    companion object {
        val bool = java.lang.Boolean::class.java.schemeK.type
        val any = java.lang.Object::class.java.schemeK.type
        val unit = quartz.lang.Unit::class.java.schemeK.type
        val function = quartz.lang.Function::class.java.schemeK.type
    }
}

val TypeK.arrow get() =
    if (this is TypeK.Apply && t1 is TypeK.Apply && t1.t1 == TypeK.function)
        TypeK.Arrow(t1.t2, t2).right()
    else
        err { "expected function, found $this" }

fun TypeK.apply(type: TypeK) = TypeK.Apply(this, type)

val Name.tVar get() = TypeK.Var(this)

val TypeK.scheme get() = SchemeK(nil, this)

/** Generalizes a type to a type scheme by pulling all type variables into constraints */
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

/** Instantiates a type scheme by replacing all constraints with fresh variables */
fun SchemeK.instantiate(): TypeK = run {
    val namesP = constraints.map { TypeK.Var(fresh()) }
    val namesZ: Subst = (constraints.map { it.name } zip namesP).toMap()
    apply(type, namesZ)
}

fun ConstraintT.constraintK(env: Env) = type
        ?.typeK(env)
        ?.map { ConstraintK(it.scheme.instantiate(), name) }
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
            .getTypeOrErr(name.qualifiedLocal)
            .map { it.scheme.instantiate() }
            .qualify()
    is TypeT.Apply -> resultMonad()
            .tupled(t1.typeK(env), t2.typeK(env))
            .map { (a, b) -> TypeK.Apply(a, b) }.ev()
            .qualify()
}

val ConstraintK.genericI get() = GenericI(name, TypeI.any)

val SchemeK.schemeI get() = SchemeI(constraints.map { it.genericI }, type.typeI)

val TypeK.typeI: TypeI get() = when (this) {
    is TypeK.Const -> ClassTypeI(name)
    is TypeK.Var -> GenericTypeI(name)
    is TypeK.Apply -> t1.typeI.apply(t2.typeI)
}
