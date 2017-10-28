package io.quartz.generator.asm

import io.quartz.tree.LocatableName
import io.quartz.tree.Name
import io.quartz.tree.Qualifier
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/** Wrapper class for ASM's ClassWriter */
class ClassGenerator(val info: ClassInfo) : ClassWriter(ClassWriter.COMPUTE_FRAMES) {
    init {
        visit(
                Opcodes.V1_8,
                info.access,
                info.name.toString(),
                info.signature,
                info.superClass.toString(),
                info.interfaces.map { it.toString() }.toTypedArray()
        )
    }

    var i = 0

    val programGeneratorLater = mutableListOf<ProgramGenerator.() -> Unit>()
    fun visitProgramGeneratorLater(fn: ProgramGenerator.() -> Unit) = programGeneratorLater.add(fn)
}

data class ClassInfo(
        val access: Int,
        val qualifier: Qualifier,
        val name: Name,
        val signature: String,
        val superClass: LocatableName,
        val interfaces: List<LocatableName>
)

fun ProgramGenerator.generateClass(info: ClassInfo, func: ClassGenerator.() -> Unit) = run {
    val cg = ClassGenerator(info)
    cg.func()
    cg.visitEnd()
    cg.programGeneratorLater.forEach { it() }
}
