package io.quartz.generator

import io.quartz.generator.asm.ProgramGenerator
import io.quartz.tree.ir.DeclI
import java.io.File

fun List<DeclI>.generate(pg: ProgramGenerator) {
    forEach { it.generate(pg) }
}

fun List<DeclI>.generate(file: File) = run {
    generate(ProgramGenerator {
        val locatableName = it.info.name
        File(file, "$locatableName.class")
                .also { it.parentFile.mkdirs() }
                .writeBytes(it.toByteArray())
    })
}
