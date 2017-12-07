package io.quartz.tree.ir

import io.quartz.err.err
import io.quartz.tree.util.*
import kategory.right

sealed class TypeI {
    data class Const(val name: QualifiedName) : TypeI()
    data class Var(val name: Name) : TypeI()
    data class Apply(val t1: TypeI, val t2: TypeI) : TypeI()

    data class Arrow(val t1: TypeI, val t2: TypeI) {
        val type get() = Apply(Apply(function, t1), t2)
    }

    fun apply(type: TypeI) = Apply(this, type)

    val arrow get() =
        if (this is TypeI.Apply && t1 is TypeI.Apply && t1.t1 == TypeI.function)
            TypeI.Arrow(t1.t2, t2).right()
        else
            err { "expected function, found $this" }

    val scheme get() = SchemeI(emptySet(), emptyList(), this)

    companion object {
        val bool = "quartz.lang.Bool".typeI
        val any = "quartz.lang.Any".typeI
        val function = "quartz.lang.Function".typeI
        fun function(arg: TypeI, value: TypeI) = function.apply(arg).apply(value)

        private val String.typeI get() = TypeI.Const(qualifiedName)
    }
}
