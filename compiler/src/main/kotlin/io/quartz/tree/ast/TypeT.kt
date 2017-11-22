package io.quartz.tree.ast

import io.quartz.nil
import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name
import io.quartz.tree.name

/** Class representing all AST constraints */
data class ConstraintT(val type: TypeT?, val name: Name)

/** Class representing all AST type schemes */
data class SchemeT(val constraints: List<ConstraintT>, val type: TypeT)

/** Sealed class representing all AST types */
sealed class TypeT : Locatable {
    data class Id(override val location: Location, val name: Name) : TypeT()

    data class Apply(override val location: Location, val t1: TypeT, val t2: TypeT) : TypeT()

    companion object {
        val unit = quartz.lang.Unit::class.java.typeT
        val function = quartz.lang.Function::class.java.typeT
        fun function(arg: TypeT, value: TypeT) = function.apply(listOf(arg, value))
    }
}

fun TypeT.apply(generics: List<TypeT>): TypeT = when (generics) {
    nil -> this
    else -> apply(generics.first()).apply(generics.drop(1))
}

fun TypeT.apply(generic: TypeT) = TypeT.Apply(location, this, generic)

val Class<*>.typeT get() = TypeT.Id(Location.unknown, name.name)
