package io.quartz.gen.jvm.asm

import io.quartz.gen.jvm.JvmGenerator
import io.quartz.tree.util.QualifiedName
import org.objectweb.asm.ClassWriter

data class ClassGenerator(
        val jg: JvmGenerator,
        val name: QualifiedName,
        val cw: ClassWriter
) {
    var i = 0
}
