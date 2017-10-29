package io.quartz.analyzer.type

import io.quartz.analyzer.EitherE
import io.quartz.analyzer.Env
import io.quartz.analyzer.fresh
import io.quartz.analyzer.monadErrorE
import io.quartz.tree.*
import io.quartz.tree.ast.DeclT
import io.quartz.tree.ast.GenericT
import io.quartz.tree.ast.SchemeT
import io.quartz.tree.ast.TypeT
import io.quartz.tree.ir.*
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
        override fun toString() = "($t1 $t2)"
    }

    /** Convenience class representing a type of (Function t1) t2 */
    data class Arrow(val t1: TypeK, val t2: TypeK) {
        val type get() = TypeK.Apply(TypeK.Apply(TypeK.function, t1), t2)
        override fun toString() = "($t1 -> $t2)"
    }

    companion object {
        val bool = java.lang.Boolean::class.java.typeK
        val any = java.lang.Object::class.java.typeK
        val unit = quartz.lang.Unit::class.java.typeK
        val function = quartz.lang.Function::class.java.typeK
    }
}

val TypeK.arrow get() = run {
    this as TypeK.Apply
    t1 as TypeK.Apply
    if (t1.t1 != TypeK.function)
        throw Exception("Expected function, found $this")
    TypeK.Arrow(t1.t2, t2)
}

val TypeK.scheme get() = SchemeK(nil, this)

/** Generalizes a type to a type scheme by pulling all type variables into generics */
fun TypeK.generalize(env: Env, subst: Subst) = SchemeK(
        freeTypeVariables.filterNot { name ->
            env.getType(name.qualifiedLocal).bimap(
                    { false },
                    { it.generics.any { it.name == name } }
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

fun GenericT.genericK() = type.typeK().map { GenericK(name, it) }

fun SchemeT.schemeK() = Either.monadErrorE().binding {
    yields(SchemeK(generics.map { it.genericK().bind() }, type.typeK().bind()))
}.ev()

fun TypeT.typeK(): EitherE<TypeK> = when (this) {
    is TypeT.Const -> TypeK.Const(name).right()
    is TypeT.Var -> TypeK.Var(name).right()
    is TypeT.Apply -> Either.monadErrorE()
            .tupled(t1.typeK(), t2.typeK())
            .map { (a, b) -> TypeK.Apply(a, b) }.ev()
}

val GenericK.genericI get() = GenericI(name, type.typeI)

val SchemeK.schemeI get() = SchemeI(generics.map { it.genericI }, type.typeI)

val TypeK.typeI: TypeI get() = when (this) {
    is TypeK.Const -> ClassTypeI(name)
    is TypeK.Var -> GenericTypeI(name)
    is TypeK.Apply -> t1.typeI.apply(t2.typeI)
}

fun DeclT.Class.schemeK(`package`: Qualifier) = SchemeK(nil, name.qualify(`package`).typeK)

val Class<*>.schemeK get() = run {
    val typeK = typeParameters.fold(TypeK.Const(qualifiedName) as TypeK) {
        a, b -> TypeK.Apply(a, TypeK.Var(b.name.name))
    }
    SchemeK(typeParameters.asIterable().map { GenericK(it.name.name, TypeK.any) }, typeK)
}

val QualifiedName.typeK get() = TypeK.Const(this)

val Class<*>.typeK get() = qualifiedName.typeK