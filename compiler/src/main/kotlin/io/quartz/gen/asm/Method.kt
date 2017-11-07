package io.quartz.gen.asm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

/** Wrapper class for ASM's GeneratorAdapter */
class MethodGenerator(
        val info: MethodInfo,
        val classGenerator: ClassGenerator,
        val ga: GeneratorAdapter
) {
    var i = 0

    val classGeneratorLater = mutableListOf<ClassGenerator.() -> Unit>()
    fun visitClassGeneratorLater(fn: ClassGenerator.() -> Unit) = classGeneratorLater.add(fn)
}

data class MethodInfo(
        val access: Int,
        val method: Method,
        val signature: String
)

fun MethodInfo.generatorAdapter(cw: ClassWriter) = GeneratorAdapter(access, method, signature, null, cw)

fun ClassGenerator.generateMethod(info: MethodInfo, fn: MethodGenerator.() -> Unit) {
    MethodGenerator(info, this, info.generatorAdapter(cw)).run {
        fn()
        ga.endMethod()
        classGeneratorLater.forEach { it() }
    }
}

fun ClassGenerator.generateConstructor(info: MethodInfo, fn: MethodGenerator.() -> Unit) {
    MethodGenerator(info, this, info.generatorAdapter(cw)).apply {
        ga.loadThis()
        ga.invokeConstructor(
                Type.getType("Ljava/lang/Object;"),
                Method.getMethod("void <init> ()")
        )
        fn()
        ga.returnValue()
        ga.endMethod()
    }
}
