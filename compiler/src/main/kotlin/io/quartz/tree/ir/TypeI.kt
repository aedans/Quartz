package io.quartz.tree.ir

import io.quartz.tree.util.*

sealed class TypeI {
    data class Const(val name: QualifiedName) : TypeI()
    data class Var(val name: Name) : TypeI()
    data class Apply(val t1: TypeI, val t2: TypeI) : TypeI()

    data class Arrow(val t1: TypeI, val t2: TypeI)

    fun apply(type: TypeI) = Apply(this, type)

    companion object {
        val any = "quartz.lang.Any".typeI
        val function = "quartz.lang.Function".typeI
        fun function(arg: TypeI, value: TypeI) = function.apply(arg).apply(value)

        private val String.typeI get() = TypeI.Const(qualifiedName)
    }
}
