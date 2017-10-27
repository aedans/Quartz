package io.quartz.generator.asm

import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

/**
 * @author Aedan Smith
 */

class MethodGenerator(
        val info: MethodInfo,
        val classGenerator: ClassGenerator
) {
    val ga = GeneratorAdapter(info.access, info.method, info.signature, null, classGenerator)

    var i = 0

    val classGeneratorLater = mutableListOf<ClassGenerator.() -> Unit>()
    fun visitClassGeneratorLater(fn: ClassGenerator.() -> Unit) = classGeneratorLater.add(fn)
}

data class MethodInfo(
        val access: Int,
        val method: Method,
        val signature: String
)

fun ClassGenerator.generateMethod(info: MethodInfo, fn: MethodGenerator.() -> Unit) {
    MethodGenerator(info, this).run {
        fn()
        ga.endMethod()
        classGeneratorLater.forEach { it() }
    }
}

fun ClassGenerator.generateConstructor(info: MethodInfo, fn: MethodGenerator.() -> Unit) {
    MethodGenerator(info, this).run {
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
