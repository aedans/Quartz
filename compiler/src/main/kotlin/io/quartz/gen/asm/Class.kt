package io.quartz.gen.asm

import io.quartz.tree.LocatableName
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/** Wrapper class for ASM's ClassWriter */
class ClassGenerator(
        val info: ClassInfo,
        val cw: ClassWriter
) {
    var i = 0

    val programGeneratorLater = mutableListOf<ProgramGenerator.() -> Unit>()
    fun visitProgramGeneratorLater(fn: ProgramGenerator.() -> Unit) = programGeneratorLater.add(fn)
}

data class ClassInfo(
        val access: Int,
        val name: LocatableName,
        val signature: String,
        val superClass: LocatableName,
        val interfaces: List<LocatableName>
)

fun ProgramGenerator.generateClass(info: ClassInfo, func: ClassGenerator.() -> Unit) {
    ClassGenerator(info, ClassWriter(ClassWriter.COMPUTE_FRAMES)).apply {
        cw.visit(
                Opcodes.V1_8,
                info.access,
                info.name.toString(),
                info.signature,
                info.superClass.toString(),
                info.interfaces.map { it.toString() }.toTypedArray()
        )
        func()
        cw.visitEnd()
        programGeneratorLater.forEach { it() }
        out(this)
    }
}
