package io.quartz.gen.jvm.asm

import io.quartz.gen.jvm.JvmGenerator
import io.quartz.gen.jvm.tree.*
import io.quartz.nil
import io.quartz.tree.util.Name
import org.objectweb.asm.*
import org.objectweb.asm.commons.*

fun JvmClass.generate(jg: JvmGenerator) = run {
    val cg = ClassGenerator(jg, name, ClassWriter(ClassWriter.COMPUTE_FRAMES))
    val access = Opcodes.ACC_PUBLIC +
            (if (isFinal) Opcodes.ACC_FINAL else 0) +
            (if (isInterface) Opcodes.ACC_INTERFACE + Opcodes.ACC_ABSTRACT else 0)
    annotations.forEach { it.generate(cg) }
    cg.run {
        cw.visit(
                Opcodes.V1_8,
                access,
                name.locatableString,
                classSignature(foralls, listOf(JvmType.`object`) + interfaces),
                "java/lang/Object",
                interfaces.map { it.qualified }.toTypedArray()
        )
        if (!isInterface)
            visitDefaultConstructor()
        decls.forEach { it.generate(cg) }
        cw.visitEnd()
        cw
    }
}

fun ClassGenerator.visitDefaultConstructor() = run {
    val ga = GeneratorAdapter(Opcodes.ACC_PUBLIC, Method.getMethod("void <init> ()"), null, emptyArray(), cw)
    ga.loadThis()
    ga.invokeConstructor(JvmType.`object`.asmType, Method.getMethod("void <init> ()"))
    ga.returnValue()
    ga.visitEnd()
}

fun classSignature(
        foralls: Set<Name>,
        superTypes: List<JvmType>
) = when (foralls) {
    nil -> ""
    else -> foralls.joinToString(prefix = "<", postfix = ">", separator = "") { "${it.string}:Ljava/lang/Object;" }
} + superTypes.joinToString(separator = "", prefix = "", postfix = "") { it.signature }
