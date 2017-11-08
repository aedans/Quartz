package io.quartz.analyze.type

import io.quartz.analyze.Env
import io.quartz.analyze.TypeInfo
import io.quartz.analyze.fresh
import io.quartz.analyze.withType
import io.quartz.err.Err
import io.quartz.err.err
import io.quartz.err.errMonad
import io.quartz.interop.schemeK
import io.quartz.nil
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.GenericT
import io.quartz.tree.ast.SchemeT
import io.quartz.tree.ast.TypeT
import io.quartz.tree.ir.*
import io.quartz.tree.qualifiedLocal
import kategory.*

/** Class representing a generic for compiler analysis */
data class GenericK(val name: Name, val type: TypeK) {
    override fun toString() = "$name :: $type"
}

/** Class representing a type scheme for compiler analysis */
data class SchemeK(val generics: List<GenericK>, val type: TypeK) {
    override fun toString() = "$generics => $type"
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

val TypeK.scheme get() = SchemeK(nil, this)

/** Generalizes a type to a type scheme by pulling all type variables into generics */
fun TypeK.generalize(env: Env, subst: Subst) = SchemeK(
        freeTypeVariables.filterNot { name ->
            env.getType(name.qualifiedLocal).bimap(
                    { false },
                    { it.scheme.generics.any { it.name == name } }
            ).fold(::identity, ::identity)
        }.map {
            GenericK(it, subst[it] ?: TypeK.any)
        },
        this
)

/** Instantiates a type scheme by replacing all generics with fresh variables */
fun SchemeK.instantiate(): TypeK = run {
    val namesP = generics.map { TypeK.Var(fresh()) }
    val namesZ: Subst = (generics.map { it.name } zip namesP).toMap()
    apply(type, namesZ)
}

fun GenericT.genericK(env: Env) = type.typeK(env).map { GenericK(name, it) }

fun SchemeT.schemeK(env: Env) = errMonad().binding {
    val localEnv = generics.localEnv(env)
    yields(SchemeK(generics.map { it.genericK(localEnv).bind() }, type.typeK(localEnv).bind()))
}.ev()

fun List<GenericT>.localEnv(env: Env) = fold(env) { envP, generic ->
    envP.withType(
            generic.name.qualifiedLocal,
            TypeInfo(TypeK.Var(generic.name).scheme).right()
    )
}

fun TypeT.typeK(env: Env): Err<TypeK> = when (this) {
    is TypeT.Id -> env
            .getType(name.qualifiedLocal)
            .map { it.scheme.instantiate() }
            .qualify()
    is TypeT.Apply -> errMonad()
            .tupled(t1.typeK(env), t2.typeK(env))
            .map { (a, b) -> TypeK.Apply(a, b) }.ev()
            .qualify()
}

val GenericK.genericI get() = GenericI(name, type.typeI)

val SchemeK.schemeI get() = SchemeI(generics.map { it.genericI }, type.typeI)

val TypeK.typeI: TypeI get() = when (this) {
    is TypeK.Const -> ClassTypeI(name)
    is TypeK.Var -> GenericTypeI(name)
    is TypeK.Apply -> t1.typeI.apply(t2.typeI)
}
