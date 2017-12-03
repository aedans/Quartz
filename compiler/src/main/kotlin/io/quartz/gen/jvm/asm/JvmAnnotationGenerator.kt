package io.quartz.gen.jvm.asm

import io.quartz.gen.jvm.tree.JvmAnnotation

fun JvmAnnotation.generate(cg: ClassGenerator) = run {
    val av = cg.cw.visitAnnotation(qualifiedName.locatableString, true)!!
    av.visitEnd()
    av
}
