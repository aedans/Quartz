package io.quartz.gen.jvm.asm

import io.quartz.gen.jvm.tree.*
import io.quartz.nil
import io.quartz.tree.util.Name
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.*

fun JvmDecl.generate(cg: ClassGenerator) = when (this) {
    is JvmDecl.Method -> generate(cg)
    is JvmDecl.Field -> generate(cg)
}

fun JvmDecl.Method.generate(cg: ClassGenerator) {
    val method = method(returnType, name, argTypes)
    val signature = methodSignature(generics, argTypes, returnType)
    val access = Opcodes.ACC_PUBLIC +
            (if (isAbstract) Opcodes.ACC_ABSTRACT else 0)
    expr?.let {
        MethodGenerator(cg, GeneratorAdapter(access, method, signature, emptyArray(), cg.cw)).run {
            it.generate(this)
            ga.returnValue()
            ga.visitEnd()
        }
    }
}

fun JvmDecl.Field.generate(cg: ClassGenerator) {
    val access = Opcodes.ACC_PUBLIC
    val fv = cg.cw.visitField(access, name.string, type.string, type.signature, null)
    fv.visitEnd()
}

fun method(returnType: JvmType, name: Name, argTypes: List<JvmType>) =
        Method.getMethod("${returnType.string} ${name.string} " +
                argTypes.joinToString(prefix = "(", postfix = ")") { it.string })!!

fun methodSignature(generics: List<JvmGeneric>, argTypes: List<JvmType>, returnType: JvmType) = when (generics) {
    nil -> ""
    else -> generics.joinToString(prefix = "<", postfix = ">", separator = "") { "${it.name.string}:${it.constraint.signature}" }
} + argTypes.joinToString(prefix = "(", postfix = ")", separator = "") { it.signature } +
        returnType.signature
