package io.quartz.gen.jvm.asm

import io.quartz.gen.jvm.tree.JvmAnnotation

fun JvmAnnotation.generate(cg: ClassGenerator) = run {
    val av = cg.cw.visitAnnotation("L${qualifiedName.locatableString};", true)!!
    args.forEach {
        av.visit(it.name.string, it.arg)
    }
    av.visitEnd()
    av
}
