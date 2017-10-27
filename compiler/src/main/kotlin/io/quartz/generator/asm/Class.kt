package io.quartz.generator.asm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * @author Aedan Smith
 */

class ClassGenerator(val info: ClassInfo) : ClassWriter(ClassWriter.COMPUTE_FRAMES) {
    init {
        visit(Opcodes.V1_8, info.access, info.name, info.signature, info.superClass, info.interfaces.toList().toTypedArray())
    }

    var i = 0

    val programGeneratorLater = mutableListOf<ProgramGenerator.() -> Unit>()
    fun visitProgramGeneratorLater(fn: ProgramGenerator.() -> Unit) = programGeneratorLater.add(fn)
}

data class ClassInfo(
        val access: Int,
        val name: String,
        val signature: String,
        val superClass: String,
        val interfaces: List<String>
)

fun ProgramGenerator.generateClass(info: ClassInfo, func: ClassGenerator.() -> Unit) = run {
    val cg = ClassGenerator(info)
    cg.func()
    cg.visitEnd()
    cg.programGeneratorLater.forEach { it() }
}
