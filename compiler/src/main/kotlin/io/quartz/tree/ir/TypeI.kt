package io.quartz.tree.ir

import io.quartz.nil
import io.quartz.tree.*

/** Class representing any IR constraintT */
data class ConstraintI(val name: Name, val type: TypeI)

/** Class representing any IR type scheme */
data class SchemeI(val constraints: List<ConstraintI>, val type: TypeI)

/** Class representing any IR type */
interface TypeI {
    val generics: List<TypeI>
    val qualifiedName: QualifiedName
    val locatableName: LocatableName
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

data class ConstTypeI(val name: QualifiedName) : TypeI {
    override val generics get() = nil
    override val qualifiedName get() = name
    override val locatableName get() = name.locatableName
    override val descriptor get() = "L$locatableName;"
    override fun getSignature(generics: List<TypeI>) = "L$locatableName" + when (generics) {
        nil -> ""
        else -> generics.joinToString(prefix = "<", postfix = ">", separator = "") { it.signature }
    } + ";"

    override fun toString() = "ConstTypeI($name)"
}

data class VarTypeI(val name: Name) : TypeI {
    override val generics get() = nil
    override val qualifiedName get() = name.qualifiedLocal
    override val locatableName get() = qualifiedName.locatableName
    override val descriptor get() = "T$name;"
    override fun getSignature(generics: List<TypeI>) = "T$name" + when (generics) {
        nil -> ""
        else -> generics.joinToString(prefix = "<", postfix = ">", separator = "") { it.signature }
    } + ";"

    override fun toString() = "VarTypeI($name)"
}

object VoidTypeI : TypeI {
    override val generics get() = nil
    override val qualifiedName get() = "void".name.qualifiedLocal
    override val locatableName get() = qualifiedName.locatableName
    override val descriptor get() = "V"
    override fun getSignature(generics: List<TypeI>) = "V"
}

val TypeI.name get() = qualifiedName.string.name

val QualifiedName.typeI get() = ConstTypeI(this)

val Class<*>.typeI: TypeI get() = qualifiedName.typeI

val TypeI.signature get() = getSignature(generics)

fun TypeI.apply(generic: TypeI): TypeI = object : TypeI by this {
    override val generics get() = (this@apply.generics + generic).toList()
    override fun toString() = "${this@apply}[$generic]"
}
