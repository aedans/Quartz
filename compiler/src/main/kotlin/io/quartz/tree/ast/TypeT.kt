package io.quartz.tree.ast

import io.quartz.tree.ir.typeI
import io.quartz.tree.nil

/**
 * @author Aedan Smith
 */

data class GenericT(val name: String, val type: TypeT)

data class SchemeT(val generics: List<GenericT>, val type: TypeT) {
    override fun toString() = "$generics => $type"
}

sealed class TypeT {
    data class Const(val name: String) : TypeT() {
        override fun toString() = name
    }

    data class Var(val name: String) : TypeT() {
        override fun toString() = name
    }

    data class Apply(val type: TypeT, val apply: TypeT) : TypeT() {
        override fun toString() = "($type) $apply"
    }

    companion object {
        val bool = java.lang.Boolean::class.java.typeT
        val byte = java.lang.Byte::class.java.typeT
        val char = java.lang.Short::class.java.typeT
        val short = java.lang.Short::class.java.typeT
        val int = java.lang.Integer::class.java.typeT
        val long = java.lang.Long::class.java.typeT
        val float = java.lang.Float::class.java.typeT
        val any = java.lang.Object::class.java.typeT
        val unit = quartz.lang.Unit::class.java.typeT
        val function = quartz.lang.Function::class.java.typeT
        fun function(arg: TypeT, value: TypeT) = function.apply(listOf(arg, value))
    }
}

fun TypeT.apply(generics: List<TypeT>): TypeT = when (generics) {
    nil -> this
    else -> apply(generics.first()).apply(generics.drop(1))
}

fun TypeT.apply(generic: TypeT) = TypeT.Apply(this, generic)

val Class<*>.typeT get() = TypeT.Const(typeName)
