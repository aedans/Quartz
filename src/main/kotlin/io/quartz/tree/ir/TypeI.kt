package io.quartz.tree.ir

import io.quartz.tree.nil

/**
 * @author Aedan Smith
 */

data class GenericI(
        val name: String,
        val type: TypeI
)

data class SchemeI(val generics: List<GenericI>, val type: TypeI)

interface TypeI {
    val generics: List<TypeI>
    val qualifiedName: String
    val locatableName: String
    val descriptor: String
    fun getSignature(generics: List<TypeI>): String

    companion object {
        val bool = java.lang.Boolean::class.java.typeI
        val byte = java.lang.Byte::class.java.typeI
        val char = java.lang.Short::class.java.typeI
        val short = java.lang.Short::class.java.typeI
        val int = java.lang.Integer::class.java.typeI
        val long = java.lang.Long::class.java.typeI
        val float = java.lang.Float::class.java.typeI
        val double = java.lang.Double::class.java.typeI
        val any = java.lang.Object::class.java.typeI
        val unit = quartz.lang.Unit::class.java.typeI
        val function = quartz.lang.Function::class.java.typeI
        fun function(arg: TypeI, value: TypeI) = function.apply(arg).apply(value)
    }
}

data class ClassTypeI(val name: String) : TypeI {
    override val generics get() = nil
    override val qualifiedName get() = name
    override val locatableName get() = name.replace('.', '/')
    override val descriptor get() = "L$locatableName;"
    override fun getSignature(generics: List<TypeI>) = "L$locatableName" + when (generics) {
        nil -> ""
        else -> generics.joinToString(prefix = "<", postfix = ">", separator = "") { it.signature }
    } + ";"

    override fun toString() = "ClassTypeI($name)"
}

data class GenericTypeI(val name: String) : TypeI {
    override val generics get() = nil
    override val qualifiedName get() = name
    override val locatableName get() = name
    override val descriptor get() = "T$name;"
    override fun getSignature(generics: List<TypeI>) = "T$name" + when (generics) {
        nil -> ""
        else -> generics.joinToString(prefix = "<", postfix = ">", separator = "") { it.signature }
    } + ";"

    override fun toString() = "GenericTypeI($name)"
}

object VoidTypeI : TypeI {
    override val generics get() = nil
    override val qualifiedName get() = "void"
    override val locatableName get() = "void"
    override val descriptor get() = "V"
    override fun getSignature(generics: List<TypeI>) = "V"
}

val String.typeI: TypeI get() = ClassTypeI(this)

val Class<*>.typeI: TypeI get() = ClassTypeI(typeName)

val TypeI.signature get() = getSignature(generics)

fun TypeI.apply(generic: TypeI): TypeI = object : TypeI by this {
    override val generics get() = (this@apply.generics + generic).toList()
    override fun toString() = "${this@apply}[$generic]"
}
