package io.quartz.tree.ast

import io.quartz.tree.util.*

sealed class TypeT : Locatable {
    data class Id(override val location: Location?, val name: QualifiedName) : TypeT()
    data class Apply(override val location: Location?, val t1: TypeT, val t2: TypeT) : TypeT()

    fun apply(generic: TypeT) = TypeT.Apply(location, this, generic)

    companion object {
        val unit = TypeT.Id(null, "quartz.lang.Unit".qualifiedName)
        private val function = TypeT.Id(null, "quartz.lang.Function".qualifiedName)
        fun function(arg: TypeT, value: TypeT) = function.apply(arg).apply(value)
    }
}
