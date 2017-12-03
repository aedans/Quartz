package io.quartz.gen.jvm.asm

import org.objectweb.asm.commons.GeneratorAdapter

data class MethodGenerator(
        val cg: ClassGenerator,
        val ga: GeneratorAdapter
)