package io.quartz.analyzer.type

import io.quartz.analyzer.Env
import io.quartz.analyzer.fresh
import io.quartz.tree.*
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ast.GenericT
import io.quartz.tree.ast.SchemeT
import io.quartz.tree.ast.TypeT
import io.quartz.tree.ir.*

/**
 * @author Aedan Smith
 */

data class GenericK(val name: Name, val type: TypeK) {
    override fun toString() = "$name :: $type"
}

data class SchemeK(val generics: List<GenericK>, val type: TypeK) {
    override fun toString() = "$generics => $type"
}

sealed class TypeK {
    data class Const(val name: QualifiedName) : TypeK() {
        override fun toString() = name.toString()
    }

    data class Var(val name: Name) : TypeK() {
        override fun toString() = name.toString()
    }

    data class Apply(val t1: TypeK, val t2: TypeK) : TypeK() {
        override fun toString() = "($t1 $t2)"
    }

    data class Arrow(val t1: TypeK, val t2: TypeK)

    companion object {
        val any = java.lang.Object::class.java.typeK
        val function = quartz.lang.Function::class.java.typeK
    }
}

val TypeK.Arrow.type get() = TypeK.Apply(TypeK.Apply(TypeK.function, t1), t2)

val TypeK.arrow get() = run {
    this as TypeK.Apply
    t1 as TypeK.Apply
    if (t1.t1 != TypeK.function)
        throw Exception("Expected function, found $this")
    TypeK.Arrow(t1.t2, t2)
}

val TypeK.scheme get() = SchemeK(nil, this)

fun TypeK.generalize(env: Env, subst: Subst) = SchemeK(
        freeTypeVariables.filterNot { name ->
            env.getType(name.qualifiedLocal).let { it != null && it.generics.any { it.name == name } }
        }.map {
            GenericK(it, subst[it] ?: TypeK.any)
        },
        this
)

fun SchemeK.instantiate(): TypeK = run {
    val namesP = generics.map { TypeK.Var(fresh()) }
    val namesZ: Subst = (generics.map { it.name } zip namesP).toMap()
    apply(namesZ, type)
}

fun GenericT.genericK(env: Env) = GenericK(name, type.typeK(env))

fun SchemeT.schemeK(env: Env) = SchemeK(generics.map { it.genericK(env) }, type.typeK(env))

fun TypeT.typeK(env: Env): TypeK = when (this) {
    is TypeT.Const -> env.getType(name)?.instantiate() ?: throw Exception(this.toString())
    is TypeT.Var -> TypeK.Var(name)
    is TypeT.Apply -> TypeK.Apply(type.typeK(env), apply.typeK(env))
}

val GenericK.genericI get() = GenericI(name, type.typeI)

val SchemeK.schemeI get() = SchemeI(generics.map { it.genericI }, type.typeI)

val TypeK.typeI: TypeI get() = when (this) {
    is TypeK.Const -> ClassTypeI(name)
    is TypeK.Var -> GenericTypeI(name)
    is TypeK.Apply -> t1.typeI.apply(t2.typeI)
}

fun DeclT.Class.schemeK(env: Env) = SchemeK(nil, name.qualify(env.`package`).typeK)

val Class<*>.schemeK get() = run {
    val typeK = typeParameters.fold(TypeK.Const(qualifiedName) as TypeK) {
        a, b -> TypeK.Apply(a, TypeK.Var(b.name.name))
    }
    SchemeK(typeParameters.asIterable().map { GenericK(it.name.name, TypeK.any) }, typeK)
}

val QualifiedName.typeK get() = TypeK.Const(this)

val Class<*>.typeK get() = qualifiedName.typeK
