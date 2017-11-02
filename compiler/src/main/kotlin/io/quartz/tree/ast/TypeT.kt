package io.quartz.tree.ast

import io.quartz.tree.Name
import io.quartz.tree.name
import io.quartz.nil

/** Class representing all AST generics */
data class GenericT(val name: Name, val type: TypeT)

/** Class representing all AST type schemes */
data class SchemeT(val generics: List<GenericT>, val type: TypeT) {
    override fun toString() = "$generics => $type"
}

/** Sealed class representing all AST types */
sealed class TypeT {
    data class Id(val name: Name) : TypeT() {
        override fun toString() = name.toString()
    }

    data class Apply(val t1: TypeT, val t2: TypeT) : TypeT() {
        override fun toString() = "($t1) $t2"
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

val Class<*>.typeT get() = TypeT.Id(name.name)
