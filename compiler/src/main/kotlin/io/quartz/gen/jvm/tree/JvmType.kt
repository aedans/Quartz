package io.quartz.gen.jvm.tree

import io.quartz.nil
import io.quartz.tree.util.*
import org.objectweb.asm.Type

sealed class JvmType {
    abstract val string: String
    abstract val descriptor: String
    abstract val qualified: String
    abstract val signature: String

    val asmType get() = Type.getType(descriptor)!!

    data class Class(val name: QualifiedName, val generics: List<JvmType> = nil) : JvmType() {
        override val string get() = name.qualifiedString
        override val descriptor = "L${name.locatableString};"
        override val qualified = name.qualifiedString
        override val signature get() = "L${name.locatableString}${when (generics) {
            nil -> ""
            else -> generics.joinToString(prefix = "<", postfix = ">", separator = "") { it.signature }
        }};"
    }

    data class Generic(val name: Name) : JvmType() {
        override val string get() = name.string
        override val descriptor = "T${name.string};"
        override val qualified = name.string
        override val signature get() = descriptor
    }

    object Void : JvmType() {
        override val string = "void"
        override val qualified = "void"
        override val descriptor = "V"
        override val signature = "V"
    }

    data class Arrow(val t1: JvmType, val t2: JvmType)

    companion object {
        val `object` = JvmType.Class("java.lang.Object".qualifiedName)
        val any = JvmType.Class("quartz.lang.Any".qualifiedName)
        val function = JvmType.Class("quartz.lang.Function".qualifiedName)
        fun function(argType: JvmType, returnType: JvmType) = function.copy(generics = listOf(argType, returnType))
    }
}
